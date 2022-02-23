package slimeknights.mantle.lib.mixin.client;

import slimeknights.mantle.lib.event.ClientWorldEvents;
import slimeknights.mantle.lib.util.MixinHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

@Environment(EnvType.CLIENT)
@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "<init>", at = @At("TAIL"))
	public void mantle$init(CallbackInfo ci) {
		ClientWorldEvents.LOAD.invoker().onWorldLoad(minecraft, MixinHelper.cast(this));
	}
}
