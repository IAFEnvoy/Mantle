package slimeknights.mantle.lib.util;

import slimeknights.mantle.lib.event.RenderTooltipBorderColorCallback;
import slimeknights.mantle.lib.mixin.accessor.ScreenAccessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class ScreenHelper {
	public static final int DEFAULT_BORDER_COLOR_START = 1347420415;
	public static final int DEFAULT_BORDER_COLOR_END = 1344798847;
	public static RenderTooltipBorderColorCallback.BorderColorEntry CURRENT_COLOR;

	public static Minecraft getClient(Screen screen) {
		return ((ScreenAccessor) screen).mantle$getMinecraft();
	}
}
