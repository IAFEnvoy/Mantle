package slimeknights.mantle.lib.mixin.client;

import slimeknights.mantle.lib.extensions.AbstractContainerScreenExtensions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

@Environment(EnvType.CLIENT)
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin implements AbstractContainerScreenExtensions {

	private Inventory mantle$inventory;

	@Override
	public Inventory mantle$getInventory() {
		return mantle$inventory;
	}

	@Inject(at = @At("RETURN"), method = "<init>")
	private void mantle$init(AbstractContainerMenu abstractContainerMenu, Inventory inventory, Component component, CallbackInfo ci) {
		mantle$inventory = inventory;
	}
}
