package slimeknights.mantle.data.loadable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Contract;
import slimeknights.mantle.data.loadable.field.DefaultingField;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.field.NullableField;
import slimeknights.mantle.data.loadable.field.RequiredField;
import slimeknights.mantle.data.loadable.mapping.ListLoadable;
import slimeknights.mantle.data.loadable.mapping.MappedLoadable;
import slimeknights.mantle.data.loadable.mapping.SetLoadable;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

/** Interface for a generic loadable object */
@SuppressWarnings("unused")  // API
public interface Loadable<T> extends JsonDeserializer<T>, JsonSerializer<T> {
  /**
   * Deserializes the object from the passed JSON element
   * @param element  Element of an unknown type to parse
   * @param key      Key that contained this element
   * @return  Parsed loadable value
   * @throws com.google.gson.JsonSyntaxException  If unable to read from JSON
   */
  T convert(JsonElement element, String key);

  /**
   * Writes the passed object to json
   * @param object  Object to serialize
   * @return  Serialized object
   * @throws RuntimeException  If unable to serialize the object
   */
  JsonElement serialize(T object);

  /**
   * Reads the object from the packet buffer
   * @param buffer  Buffer instance
   * @return  Instance read from network
   * @throws io.netty.handler.codec.DecoderException  If unable to decode a value from network
   */
  T fromNetwork(FriendlyByteBuf buffer);

  /**
   * Writes this object to the packet buffer
   * @param object  Object to write
   * @param buffer  Buffer instance
   * @throws io.netty.handler.codec.EncoderException  If unable to encode a value to network
   */
  void toNetwork(T object, FriendlyByteBuf buffer);


  /* GSON methods, lets us easily use loadables with GSON adapters. Generally should not override. */

  @Override
  default T deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
    return convert(json, type.getTypeName());
  }

  @Override
  default JsonElement serialize(T object, Type type, JsonSerializationContext context) {
    return serialize(object);
  }


  /* Helpers for raw loadable use */

  /**
   * Gets then deserializes the given fieldm throwing if it is missing
   * You should not override this method as we wish to leave that handling missing up to the RecordLoadable.
   * Instead, consider a custom implementation of defaultField if you have a standard default.
   * @param parent  Parent to fetch field from
   * @param key     Field to get
   * @return  Value, or throws if missing
   * @throws JsonSyntaxException  If the field is missing or cannot be parsed.
   */
  default T getIfPresent(JsonObject parent, String key) {
    if (parent.has(key)) {
      return convert(parent.get(key), key);
    }
    throw new JsonSyntaxException("Missing JSON field " + key + "");
  }

  /**
   * Gets then deserializes the given field, or returns a default value if its missing.
   * You should not override this method as we wish to leave that handling missing up to the RecordLoadable.
   * Instead, consider a custom implementation of defaultField if you have a standard default.
   * @param parent        Parent to fetch field from
   * @param key           Field to get
   * @param defaultValue  Default value to fetch
   * @return  Value or default.
   * @throws JsonSyntaxException  If the field cannot be parsed.
   */
  @Nullable
  @Contract("_,_,!null->!null")
  default T getOrDefault(JsonObject parent, String key, @Nullable T defaultValue) {
    JsonElement element = parent.get(key);
    if (element != null && !element.isJsonNull()) {
      return convert(element, key);
    }
    return defaultValue;
  }


  /* Fields */

  /** Creates a required field from this loadable */
  default <P> LoadableField<T,P> requiredField(String key, Function<P,T> getter) {
    return new RequiredField<>(this, key, false, getter);
  }

  /** Creates an optional field that falls back to null */
  default <P> LoadableField<T,P> nullableField(String key, Function<P,T> getter) {
    return new NullableField<>(this, key, getter);
  }

  /** Creates a defaulting field that uses a default value when missing */
  default <P> LoadableField<T,P> defaultField(String key, T defaultValue, boolean serializeDefault, Function<P,T> getter) {
    return new DefaultingField<>(this, key, defaultValue, serializeDefault, getter);
  }

  /** Creates a defaulting field that uses a default value when missing */
  default <P> LoadableField<T,P> defaultField(String key, T defaultValue, Function<P,T> getter) {
    return defaultField(key, defaultValue, false, getter);
  }


  /* Builders */

  /** Makes a list of this loadable */
  default Loadable<List<T>> list(int minSize) {
    return new ListLoadable<>(this, minSize);
  }

  /** Makes a list of this loadable */
  default Loadable<List<T>> list() {
    return list(1);
  }

  /** Makes a set of this loadable */
  default Loadable<Set<T>> set(int minSize) {
    return new SetLoadable<>(this, minSize);
  }

  /** Makes a set of this loadable */
  default Loadable<Set<T>> set() {
    return set(1);
  }


  /* Mapping */

  /** Maps this loader to another type, with error factory on both from and to */
  default <M> Loadable<M> map(BiFunction<T,ErrorFactory,M> from, BiFunction<M,ErrorFactory,T> to) {
    return MappedLoadable.of(this, from, to);
  }

  /** Maps this loader to another type, with error factory on from */
  default <M> Loadable<M> comapFlatMap(BiFunction<T,ErrorFactory,M> from, Function<M,T> to) {
    return map(from, MappedLoadable.flatten(to));
  }

  /** Maps this loader to another type */
  default <M> Loadable<M> flatComap(Function<T,M> from, BiFunction<M,ErrorFactory,T> to) {
    return map(MappedLoadable.flatten(from), to);
  }

  /** Maps this loader to another type */
  default <M> Loadable<M> flatMap(Function<T,M> from, Function<M,T> to) {
    return map(MappedLoadable.flatten(from), MappedLoadable.flatten(to));
  }
}