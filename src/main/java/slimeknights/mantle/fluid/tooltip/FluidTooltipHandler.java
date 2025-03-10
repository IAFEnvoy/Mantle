package slimeknights.mantle.fluid.tooltip;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.fabricators_of_create.porting_lib.event.common.TagsUpdatedCallback;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import lombok.extern.log4j.Log4j2;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.material.Fluid;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.SafeClientAccess;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.mantle.data.TagKeySerializer;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;
import slimeknights.mantle.util.JsonHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiConsumer;

/** Handles fluid units displaying in tooltips */
@SuppressWarnings("unused")
@Log4j2
public class FluidTooltipHandler extends SimpleJsonResourceReloadListener implements IdentifiableResourceReloadListener {
  /** Tooltip when not holding shift mentioning that is possible */
  public static final Component HOLD_SHIFT = Mantle.makeComponent("gui", "fluid.hold_shift").withStyle(ChatFormatting.GRAY);
  /** Folder for saving the logic */
  public static final String FOLDER = "mantle/fluid_tooltips";
  /** GSON instance */
  public static final Gson GSON = (new GsonBuilder())
    .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
    .registerTypeAdapter(FluidIngredient.class, FluidIngredient.SERIALIZER)
    .registerTypeAdapter(TagKey.class, new TagKeySerializer<>(Registries.FLUID))
    .setPrettyPrinting()
    .disableHtmlEscaping()
    .create();

  /** ID of the default fallback */
  public static final ResourceLocation DEFAULT_ID = Mantle.getResource("fallback");

  /* Base units */
  private static final FluidUnit BUCKET = new FluidUnit(Mantle.makeDescriptionId("gui", "fluid.bucket"), 10000);
  private static final FluidUnit MILLIBUCKET = new FluidUnit(Mantle.makeDescriptionId("gui", "fluid.millibucket"), 81);
  private static final FluidUnit DROPLET = new FluidUnit(Mantle.makeDescriptionId("gui", "fluid.droplet"), 1);
  /** Default fallback in case resource pack has none */
  private static final FluidUnitList DEFAULT_LIST = new FluidUnitList(null, List.of(BUCKET, DROPLET));

  /** Formatter as a biconsumer, shows up in a few places */
  public static final BiConsumer<Long,List<Component>> BUCKET_FORMATTER = FluidTooltipHandler::appendBuckets;

  /* Instance data */
  public static final FluidTooltipHandler INSTANCE = new FluidTooltipHandler();

  /** Fallback to use when no list matches */
  private FluidUnitList fallback = DEFAULT_LIST;
  /** List of tooltip options */
  private Map<ResourceLocation,FluidUnitList> unitLists = Collections.emptyMap();
  /** Cache of fluid to entry */
  private final Map<Fluid,FluidUnitList> listCache = new HashMap<>();

  /**
   * Initializes this manager, registering it with the resource manager
   */
  public static void init() {
    ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(INSTANCE);
    // clear the cache on tag reload, if the tags changed it might be wrong
    TagsUpdatedCallback.EVENT.register(registries -> INSTANCE.listCache.clear());
  }

  private FluidTooltipHandler() {
    super(GSON, FOLDER);
  }

  /** Loads from JSON */
  @Nullable
  private static FluidUnitList loadList(ResourceLocation key, JsonElement json) {
    try {
      return GSON.fromJson(json, FluidUnitList.class);
    } catch (JsonSyntaxException e) {
      log.error("Failed to load fluid container transfer info from {}", key, e);
      return null;
    }
  }

  @Override
  protected void apply(Map<ResourceLocation,JsonElement> splashList, ResourceManager manager, ProfilerFiller profiler) {
    long time = System.nanoTime();
    ImmutableMap.Builder<ResourceLocation,FluidUnitList> builder = ImmutableMap.builder();
    Map<ResourceLocation,ResourceLocation> redirects = new HashMap<>();
    for (Entry<ResourceLocation,JsonElement> entry : splashList.entrySet()) {
      ResourceLocation key = entry.getKey();
      JsonElement element = entry.getValue();

      // if a redirect, store in the map for later
      if (element.isJsonObject()) {
        JsonObject object = element.getAsJsonObject();
        if (object.has("redirect")) {
          ResourceLocation redirect = JsonHelper.getResourceLocation(object, "redirect");
          redirects.put(key, redirect);
          continue;
        }
      }
      // parse list regularly
      FluidUnitList list = loadList(key, element);
      if (list != null) {
        builder.put(key, list);
      }
    }
    // process redirects
    Map<ResourceLocation,FluidUnitList> mapBeforeRedirects = builder.build();
    builder = ImmutableMap.builder();
    builder.putAll(mapBeforeRedirects);
    for (Entry<ResourceLocation,ResourceLocation> entry : redirects.entrySet()) {
      ResourceLocation from = entry.getKey();
      ResourceLocation to = entry.getValue();
      FluidUnitList list = mapBeforeRedirects.get(to);
      if (list != null) {
        builder.put(from, list);
      } else {
        log.error("Invalid fluid tooltip redirect {} as unit list {} does not exist", from, to);
      }
    }
    // find the fallback
    unitLists = builder.build();
    fallback = this.unitLists.getOrDefault(DEFAULT_ID, DEFAULT_LIST);
    listCache.clear();
    log.info("Loaded {} fluid unit lists in {} ms", listCache.size(), (System.nanoTime() - time) / 1000000f);
  }

  /** Gets the unit list for the given fluid */
  private FluidUnitList getUnitList(Fluid fluid) {
    FluidUnitList cached = listCache.get(fluid);
    if (cached != null) {
      return cached;
    }
    for (FluidUnitList list : unitLists.values()) {
      if (list.matches(fluid)) {
        listCache.put(fluid, list);
        return list;
      }
    }
    listCache.put(fluid, fallback);
    return fallback;
  }

  /** Gets the unit list for the given ID */
  private FluidUnitList getUnitList(ResourceLocation id) {
    return unitLists.getOrDefault(id, fallback);
  }


  /* External utilities */

  /**
   * Gets the tooltip for a fluid stack
   * @param fluid  Fluid stack instance
   * @return  Fluid tooltip
   */
  public static List<Component> getFluidTooltip(FluidStack fluid) {
    return getFluidTooltip(fluid, fluid.getAmount());
  }

  /**
   * Gets the tooltip for a fluid stack
   * @param fluid  Fluid stack instance
   * @param amount Amount override
   * @return  Fluid tooltip
   */
  public static List<Component> getFluidTooltip(FluidStack fluid, long amount) {
    List<Component> tooltip = new ArrayList<>();
    // fluid name, not sure if there is a cleaner way to do this
    tooltip.add(fluid.getDisplayName().plainCopy().withStyle(ChatFormatting.WHITE));
    // material
    appendMaterial(fluid.getFluid(), amount, tooltip);
    // add mod display name
    FabricLoader.getInstance().getModContainer(Objects.requireNonNull(BuiltInRegistries.FLUID.getKey(fluid.getFluid())).getNamespace())
           .map(container -> container.getMetadata().getName())
           .ifPresent(name -> tooltip.add(Component.literal(name).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC)));
    return tooltip;
  }

  /**
   * Adds information for the tooltip based on material units
   * @param fluid    Input fluid stack
   * @param tooltip  Tooltip to append information
   */
  public static void appendMaterial(FluidStack fluid, List<Component> tooltip) {
    appendMaterial(fluid.getFluid(), fluid.getAmount(), tooltip);
  }

  /**
   * Adds information for the tooltip based on material units
   * @param fluid      Input fluid
   * @param amount     Input amount
   * @param tooltip    Tooltip to append information
   */
  public static void appendMaterial(Fluid fluid, long amount, List<Component> tooltip) {
    if (appendMaterialNoShift(fluid, amount, tooltip)) {
      appendShift(tooltip);
    }
  }

  /**
   * Adds information for the tooltip based on material units, does not show "hold shift for buckets"
   * @param fluid      Input fluid
   * @param original   Input amount
   * @param tooltip    Tooltip to append information
   * @return  True if the amount is not in buckets
   */
  public static boolean appendMaterialNoShift(Fluid fluid, long original, List<Component> tooltip) {
    // if holding shift, skip specific units
    if (SafeClientAccess.getTooltipKey() != TooltipKey.SHIFT) {
      long amount = original;
      amount = INSTANCE.getUnitList(fluid).getText(tooltip, amount);
      MILLIBUCKET.getText(tooltip, amount);
      return INSTANCE.listCache.get(fluid) != INSTANCE.fallback;
    } else {
      // standard display stuff: bucket amounts
      appendBuckets(original, tooltip);
      return false;
    }
  }

  /**
   * Appends the hold shift message to the tooltip
   * @param tooltip  Tooltip to append information
   */
  public static void appendShift(List<Component> tooltip) {
    if(!SafeClientAccess.getTooltipKey().isShiftOrUnknown()) {
      tooltip.add(Component.empty());
      tooltip.add(HOLD_SHIFT);
    }
  }

  /**
   * Adds information to the tooltip based on a named list, allows customizing display for a specific location
   * @param id       ID of the list to append
   * @param amount   Fluid amount
   * @param tooltip  Tooltip to append information
   */
  public static void appendNamedList(ResourceLocation id, long amount, List<Component> tooltip) {
    amount = INSTANCE.getUnitList(id).getText(tooltip, amount);
    appendBuckets(amount, tooltip);
  }

  /**
   * Adds information to the tooltip based on the fluid using bucket units
   * @param amount     Fluid amount
   * @param tooltip  Tooltip to append information
   */
  public static void appendBuckets(long amount, List<Component> tooltip) {
    amount = INSTANCE.fallback.getText(tooltip, amount);
    MILLIBUCKET.getText(tooltip, amount);
  }

  @Override
  public ResourceLocation getFabricId() {
    return Mantle.getResource("fluid_tooltip_handler");
  }
}
