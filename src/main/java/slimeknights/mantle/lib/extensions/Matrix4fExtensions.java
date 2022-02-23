package slimeknights.mantle.lib.extensions;

import com.mojang.math.Matrix4f;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface Matrix4fExtensions {
	void mantle$set(@NotNull Matrix4f other);

	@Contract(mutates = "this")
	void mantle$fromFloatArray(float[] floats);

	float[] mantle$writeMatrix();

	void mantle$setTranslation(float x, float y, float z);
}
