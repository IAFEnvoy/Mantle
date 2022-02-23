package slimeknights.mantle.lib.util;

import slimeknights.mantle.lib.extensions.BlockEntityExtensions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TileEntityHelper {
	public static final String EXTRA_DATA_KEY = "ForgeData";

	public static CompoundTag getExtraCustomData(BlockEntity tileEntity) {
		return ((BlockEntityExtensions) tileEntity).mantle$getExtraCustomData();
	}
}
