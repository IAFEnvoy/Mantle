package slimeknights.mantle.lib.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;

@Mixin(ClientboundPlayerAbilitiesPacket.class)
public interface ClientboundPlayerAbilitiesPacketAccessor {
	@Accessor("flyingSpeed")
	void mantle$setFlyingSpeed(float speed);
}
