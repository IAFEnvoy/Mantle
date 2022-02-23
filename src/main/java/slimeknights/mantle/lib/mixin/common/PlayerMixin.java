package slimeknights.mantle.lib.mixin.common;

import slimeknights.mantle.lib.block.HarvestableBlock;
import slimeknights.mantle.lib.event.PlayerTickEndCallback;
import slimeknights.mantle.lib.util.MixinHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
	@Final
	@Shadow
	private Inventory inventory;

	protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(method = "hasCorrectToolForDrops", at = @At("HEAD"), cancellable = true)
	public void mantle$isUsingEffectiveTool(BlockState blockState, CallbackInfoReturnable<Boolean> cir) {
		if (blockState.getBlock() instanceof HarvestableBlock harvestable && inventory.getSelected().getItem() instanceof DiggerItem digger) {
			cir.setReturnValue(harvestable.isToolEffective(blockState, digger));
		}
	}

	@Inject(method = "tick", at = @At("TAIL"))
	public void mantle$clientEndOfTickEvent(CallbackInfo ci) {
		PlayerTickEndCallback.EVENT.invoker().onEndOfPlayerTick(MixinHelper.cast(this));
	}
}
