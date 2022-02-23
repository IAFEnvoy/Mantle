package slimeknights.mantle.lib.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.dimension.DimensionType;

@Mixin(DimensionType.class)
public interface DimensionTypeAccessor {
	@Accessor("DEFAULT_OVERWORLD")
	static DimensionType mantle$getDefaultOverworld() {
		throw new AssertionError("Mixin application failed!");
	}
}
