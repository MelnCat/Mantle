package slimeknights.mantle.data.predicate.damage;

import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.item.enchantment.ThornsEnchantment;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.data.predicate.PredicateRegistry;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IGenericLoader;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.tags.DamageTypeTags;
import static slimeknights.mantle.data.registry.GenericLoaderRegistry.SingletonLoader.singleton;

/**
 * Predicate testing for damage sources
 */
public interface DamageSourcePredicate extends IJsonPredicate<DamageSource> {
  /** Predicate that matches all sources */
  DamageSourcePredicate ANY = simple(source -> true);
  /** Loader for item predicates */
  PredicateRegistry<DamageSource> LOADER = new PredicateRegistry<>("Damage Source Predicate", ANY);

  /* Vanilla getters */
  DamageSourcePredicate PROJECTILE = simple(x -> x.is(DamageTypeTags.IS_PROJECTILE));
  DamageSourcePredicate EXPLOSION = simple(x -> x.is(DamageTypeTags.IS_EXPLOSION));;
  DamageSourcePredicate BYPASS_ARMOR = simple(x -> x.is(DamageTypeTags.BYPASSES_ARMOR));;
  DamageSourcePredicate DAMAGE_HELMET = simple(x -> x.is(DamageTypeTags.DAMAGES_HELMET));;
  DamageSourcePredicate BYPASS_INVULNERABLE = simple(x -> x.is(DamageTypeTags.BYPASSES_INVULNERABILITY));;
  DamageSourcePredicate BYPASS_MAGIC = simple(x -> x.is(DamageTypeTags.BYPASSES_EFFECTS));;
  DamageSourcePredicate BYPASS_ENCHANTMENTS = simple(x -> x.is(DamageTypeTags.BYPASSES_ENCHANTMENTS));;
  DamageSourcePredicate FIRE = simple(x -> x.is(DamageTypeTags.IS_FIRE));
  DamageSourcePredicate MAGIC = simple(x -> x.is(DamageTypeTags.IS_PROJECTILE));;
  DamageSourcePredicate FALL = simple(x -> x.is(DamageTypeTags.IS_FALL));

  /** Damage that protection works against */
  DamageSourcePredicate CAN_PROTECT = simple(x ->
    x.is(DamageTypeTags.IS_PROJECTILE) &&
      x.is(DamageTypeTags.BYPASSES_ENCHANTMENTS) &&
      x.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
  );

  /** Custom concept: damage dealt by non-projectile entities */
  DamageSourcePredicate MELEE = simple(source -> {
    if (source.is(DamageTypeTags.IS_PROJECTILE)) {
      return false;
    }
    // if it's caused by an entity, require it to simply not be thorns
    // meets most normal melee attacks, like zombies, but also means a melee fire or melee magic attack will work
    if (source.getEntity() != null) {
      return !source.is(DamageTypes.THORNS);
    } else {
      // for non-entity damage, require it to not be any other type
      // blocks fall damage, falling blocks, cactus, but not starving, drowning, freezing
      return !source.is(DamageTypeTags.BYPASSES_ARMOR) && !source.is(DamageTypeTags.IS_FIRE) && !source.is(DamageTypeTags.BYPASSES_EFFECTS) && !source.is(DamageTypeTags.IS_EXPLOSION);
    }
  });

  @Override
  default IJsonPredicate<DamageSource> inverted() {
    return LOADER.invert(this);
  }

  /** Creates a simple predicate with no parameters */
  static DamageSourcePredicate simple(Predicate<DamageSource> predicate) {
    return singleton(loader -> new DamageSourcePredicate() {
      @Override
      public boolean matches(DamageSource source) {
        return predicate.test(source);
      }

      @Override
      public IGenericLoader<? extends DamageSourcePredicate> getLoader() {
        return loader;
      }
    });
  }


  /* Helper methods */

  /** Creates an and predicate */
  @SafeVarargs
  static IJsonPredicate<DamageSource> and(IJsonPredicate<DamageSource>... predicates) {
    return LOADER.and(List.of(predicates));
  }

  /** Creates an or predicate */
  @SafeVarargs
  static IJsonPredicate<DamageSource> or(IJsonPredicate<DamageSource>... predicates) {
    return LOADER.or(List.of(predicates));
  }
}
