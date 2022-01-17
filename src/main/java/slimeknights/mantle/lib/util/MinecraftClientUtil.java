package slimeknights.mantle.lib.util;

import slimeknights.mantle.lib.extensions.LanguageInfoExtensions;
import slimeknights.mantle.lib.mixin.accessor.MinecraftAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import java.util.Locale;

import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public final class MinecraftClientUtil {
	public static float getRenderPartialTicksPaused(Minecraft minecraft) {
		return get(minecraft).create$pausePartialTick();
	}

	public static Locale getLocale() {
		return ((LanguageInfoExtensions) Minecraft.getInstance().getLanguageManager().getSelected()).create$getJavaLocale();
	}

	private static MinecraftAccessor get(Minecraft minecraft) {
		return MixinHelper.cast(minecraft);
	}

	private MinecraftClientUtil() {}
}
