package slimeknights.mantle.data.registry;

import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.registration.object.IdAwareObject;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generic registry of a {@link IdAwareObject}.
 * @param <T> Type of the component being registered.
 */
public class IdAwareComponentRegistry<T extends IdAwareObject> extends AbstractNamedComponentRegistry<T> {
  /** Registered box expansion types */
  private final Map<ResourceLocation,T> values = new ConcurrentHashMap<>();

  public IdAwareComponentRegistry(String errorText) {
    super(errorText);
  }

  /** Registers the value with the given name */
  public <V extends T> V register(V value) {
    ResourceLocation name = value.getId();
    if (values.putIfAbsent(name, value) != null) {
      throw new IllegalArgumentException("Duplicate registration " + name);
    }
    return value;
  }

  /** Gets a value or null if missing */
  @Override
  @Nullable
  public T getValue(ResourceLocation name) {
    return values.get(name);
  }

  @Override
  public ResourceLocation getKey(T object) {
    return object.getId();
  }
}
