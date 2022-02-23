package slimeknights.mantle.lib.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import java.util.List;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

@Mixin(Entity.class)
public interface EntityAccessor {
	@Accessor("removalReason")
	void mantle$setRemovalReason(Entity.RemovalReason removalReason);

	@Invoker("getEncodeId")
	String mantle$getEntityString();

	@Invoker("collideWithShapes")
	static Vec3 mantle$collideWithShapes(Vec3 vec3, AABB aABB, List<VoxelShape> list) {
		throw new AssertionError("Mixin application failed!");
	}
}
