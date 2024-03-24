package slimeknights.mantle.data.loadable.mapping;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.data.loadable.ErrorFactory;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import java.util.function.BiFunction;
import java.util.function.Function;

/** Represents a trivially mapped loadable that serializes/writes to network like another loadable */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class MappedLoadable<F,T> implements Loadable<T> {
  private final Loadable<F> base;
  protected final BiFunction<F,ErrorFactory,T> from;
  protected final BiFunction<T,ErrorFactory,F> to;

  /** Creates a new loadable for a non-record loadable */
  public static <T,F> Loadable<T> of(Loadable<F> base, BiFunction<F,ErrorFactory,T> from, BiFunction<T,ErrorFactory,F> to) {
    return new MappedLoadable<>(base, from, to);
  }

  /** Creates a new loadable for a record loadable */
  public static <T,F> RecordLoadable<T> of(RecordLoadable<F> base, BiFunction<F,ErrorFactory,T> from, BiFunction<T,ErrorFactory,F> to) {
    return new Record<>(base, from, to);
  }

  /** Flattens the given mapping function */
  public static <T,R> BiFunction<T,ErrorFactory,R> flatten(Function<T,R> function) {
    return (value, error) -> function.apply(value);
  }

  @Override
  public T convert(JsonElement element, String key) {
    return from.apply(base.convert(element, key), ErrorFactory.JSON_SYNTAX_ERROR);
  }

  @Override
  public JsonElement serialize(T object) {
    return base.serialize(to.apply(object, ErrorFactory.RUNTIME));
  }

  @Override
  public T fromNetwork(FriendlyByteBuf buffer) {
    return from.apply(base.fromNetwork(buffer), ErrorFactory.DECODER_EXCEPTION);
  }

  @Override
  public void toNetwork(T object, FriendlyByteBuf buffer) {
    base.toNetwork(to.apply(object, ErrorFactory.ENCODER_EXCEPTION), buffer);
  }

  /** Implementation for records */
  private static class Record<F,T> extends MappedLoadable<F,T> implements RecordLoadable<T> {
    private final RecordLoadable<F> base;
    protected Record(RecordLoadable<F> base, BiFunction<F,ErrorFactory,T> from, BiFunction<T,ErrorFactory,F> to) {
      super(base, from, to);
      this.base = base;
    }

    @Override
    public T deserialize(JsonObject json) {
      return from.apply(base.deserialize(json), ErrorFactory.JSON_SYNTAX_ERROR);
    }

    @Override
    public void serialize(T object, JsonObject json) {
      base.serialize(to.apply(object, ErrorFactory.RUNTIME), json);
    }
  }
}