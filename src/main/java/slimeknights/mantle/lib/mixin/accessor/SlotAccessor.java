package slimeknights.mantle.lib.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.inventory.Slot;

@Mixin(Slot.class)
public interface SlotAccessor {
	@Accessor("slot")
	int mantle$getSlotIndex();
}
