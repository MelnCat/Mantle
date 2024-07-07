package slimeknights.mantle.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.Mantle;

import java.util.concurrent.CompletableFuture;

import static slimeknights.mantle.datagen.MantleTags.Fluids.LAVA;
import static slimeknights.mantle.datagen.MantleTags.Fluids.WATER;

/** Provider for tags added by mantle, generally not useful for other mods */
public class MantleFluidTagProvider extends FluidTagsProvider {
  public MantleFluidTagProvider(PackOutput out, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper existingFileHelper) {
    super(out, provider, Mantle.modId, existingFileHelper);
  }

  @Override
  protected void addTags(HolderLookup.Provider pProvider) {
    this.tag(WATER).add(Fluids.WATER, Fluids.FLOWING_WATER);
    this.tag(LAVA).add(Fluids.LAVA, Fluids.FLOWING_LAVA);
  }

  @Override
  public String getName() {
    return "Mantle Fluid Tag Provider";
  }
}
