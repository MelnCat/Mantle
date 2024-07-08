package slimeknights.mantle.client.screen.book;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import slimeknights.mantle.client.screen.book.element.BookElement;

public interface ILayerRenderFunction {
  void render(BookElement element, GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks);
}
