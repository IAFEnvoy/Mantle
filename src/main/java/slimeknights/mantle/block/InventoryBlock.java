package slimeknights.mantle.block;

import io.github.fabricators_of_create.porting_lib.util.NetworkHooks;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import slimeknights.mantle.block.entity.INameableMenuProvider;
import slimeknights.mantle.block.entity.InventoryBlockEntity;
import slimeknights.mantle.inventory.BaseContainerMenu;

import javax.annotation.Nullable;

/**
 * Base class for blocks with an inventory
 */
@SuppressWarnings("WeakerAccess")
public abstract class InventoryBlock extends Block implements EntityBlock {

  protected InventoryBlock(BlockBehaviour.Properties builder) {
    super(builder);
  }

  /**
   * Called when the block is activated to open the UI. Override to return false for blocks with no inventory
   * @param player Player instance
   * @param world  World instance
   * @param pos    Block position
   * @return true if the GUI opened, false if not
   */
  protected boolean openGui(Player player, Level world, BlockPos pos) {
    if (!world.isClientSide()) {
      MenuProvider container = this.getMenuProvider(world.getBlockState(pos), world, pos);
      if (container != null && player instanceof ServerPlayer serverPlayer) {
        NetworkHooks.openScreen(serverPlayer, container, pos);
        if (player.containerMenu instanceof BaseContainerMenu<?> menu) {
          menu.syncOnOpen(serverPlayer);
        }
      }
    }

    return true;
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTraceResult) {
    if (player.isSuppressingBounce()) {
      return InteractionResult.PASS;
    }
    if (!world.isClientSide) {
      return this.openGui(player, world, pos) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }
    return InteractionResult.SUCCESS;
  }


  /* Naming */

  @Override
  public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
    super.setPlacedBy(worldIn, pos, state, placer, stack);

    // set custom name from named stack
    if (stack.hasCustomHoverName()) {
      BlockEntity tileentity = worldIn.getBlockEntity(pos);
      if (tileentity instanceof INameableMenuProvider provider) {
        provider.setCustomName(stack.getHoverName());
      }
    }
  }

  @SuppressWarnings("deprecation")
  @Override
  @Nullable
  @Deprecated
  public MenuProvider getMenuProvider(BlockState state, Level worldIn, BlockPos pos) {
    BlockEntity be = worldIn.getBlockEntity(pos);
    return be instanceof MenuProvider ? (MenuProvider) be : null;
  }


  /* Inventory handling */

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
    if (state.getBlock() != newState.getBlock()) {
      Storage<ItemVariant> inventory = ItemStorage.SIDED.find(worldIn, pos, null);
      if (inventory != null)
        dropInventoryItems(state, worldIn, pos, inventory);
      if (worldIn.getBlockEntity(pos) != null)
        worldIn.updateNeighbourForOutputSignal(pos, this);
    }

    super.onRemove(state, worldIn, pos, newState, isMoving);
  }

  /**
   * Called when the block is replaced to drop contained items.
   * @param state       Block state
   * @param worldIn     Tile world
   * @param pos         Tile position
   * @param inventory   Item handler
   */
  protected void dropInventoryItems(BlockState state, Level worldIn, BlockPos pos, Storage<ItemVariant> inventory) {
    dropInventoryItems(worldIn, pos, inventory);
  }

  /**
   * Drops all items from the given inventory in world
   * @param world      World instance
   * @param pos        Position to drop
   * @param inventory  Inventory instance
   */
  public static void dropInventoryItems(Level world, BlockPos pos, Storage<ItemVariant> inventory) {
    double x = pos.getX();
    double y = pos.getY();
    double z = pos.getZ();
    for(StorageView<ItemVariant> view : inventory) {
      Containers.dropItemStack(world, x, y, z, view.getResource().toStack((int) Math.max(view.getResource().getItem().getMaxStackSize(), view.getAmount())));
    }
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public boolean triggerEvent(BlockState state, Level worldIn, BlockPos pos, int id, int param) {
    super.triggerEvent(state, worldIn, pos, id, param);
    BlockEntity be = worldIn.getBlockEntity(pos);
    return be != null && be.triggerEvent(id, param);
  }

  @Override
  public void playerDestroy(Level world, Player player, BlockPos pos, BlockState blockState, @Nullable BlockEntity blockEntity, ItemStack itemStack) {
    super.playerDestroy(world, player, pos, blockState, blockEntity, itemStack);
    if (blockEntity instanceof InventoryBlockEntity castingBlock)
      castingBlock.dropStacks();
  }
}
