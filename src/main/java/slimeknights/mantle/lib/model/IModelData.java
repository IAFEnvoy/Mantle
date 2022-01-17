package slimeknights.mantle.lib.model;

import org.jetbrains.annotations.Nullable;

public interface IModelData {
  boolean hasProperty(ModelProperty<?> prop);

  @Nullable
  <T> T getData(ModelProperty<T> prop);

  @Nullable
  <T> T setData(ModelProperty<T> prop, T data);
}
