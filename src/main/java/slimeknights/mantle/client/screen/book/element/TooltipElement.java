package slimeknights.mantle.client.screen.book.element;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;

public class TooltipElement extends SizedBookElement {

  private final List<Component> tooltips;

  public TooltipElement(List<Component> tooltip, int x, int y, int width, int height) {
    super(x, y, width, height);

    this.tooltips = tooltip;
  }

  @Override
  public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
  }

  @Override
  public void drawOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
    if (this.isHovered(mouseX, mouseY)) {
      guiGraphics.renderTooltip(mc.font, this.tooltips, Optional.empty(), mouseX, mouseY);
    }
  }
}
