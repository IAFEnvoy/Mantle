package slimeknights.mantle.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;

// TODO: class needs some rewrites
public class ScalableElementScreen extends ElementScreen {

  public ScalableElementScreen(int x, int y, int w, int h, int texW, int texH) {
    super(x, y, w, h, texW, texH);
  }

  public ScalableElementScreen(int x, int y, int w, int h) {
    super(x, y, w, h);
  }

  public int drawScaledX(GuiGraphics guiGraphics, ResourceLocation texture, int xPos, int yPos, int width) {
    for (int i = 0; i < width / this.w; i++) {
      this.draw(guiGraphics, texture, xPos + i * this.w, yPos);
    }
    // remainder that doesn't fit total width
    int remainder = width % this.w;
    if (remainder > 0) {
      guiGraphics.blit(texture, xPos + width - remainder, yPos, this.x, this.y, remainder, this.h, this.texW, this.texH);
    }

    return width;
  }

  public int drawScaledY(GuiGraphics guiGraphics, ResourceLocation texture, int xPos, int yPos, int height) {
    for (int i = 0; i < height / this.h; i++) {
      this.draw(guiGraphics, texture, xPos, yPos + i * this.h);
    }
    // remainder that doesn't fit total width
    int remainder = height % this.h;
    if (remainder > 0) {
      guiGraphics.blit(texture, xPos, yPos + height - remainder, this.x, this.y, this.w, remainder, this.texW, this.texH);
    }

    return this.w;
  }

  /**
   * Draws this shape scaling upwards instead of downwards
   * @param guiGraphics  Gui graphics instance
   * @param texture      The texture to draw
   * @param xPos         X position of top of image
   * @param yPos         Y position of top of image
   * @param height       Height to draw
   * @return  Width for some reason
   */
  public int drawScaledYUp(GuiGraphics guiGraphics, ResourceLocation texture, int xPos, int yPos, int height) {
    // remainder that doesn't fit total height
    int remainder = height % this.h;
    int offset = this.h - remainder;
    if (remainder > 0) {
      guiGraphics.blit(texture, xPos, yPos + offset, this.x, this.y + offset, this.w, remainder, this.texW, this.texH);
    }

    return this.w;
  }

  public int drawScaled(GuiGraphics guiGraphics, ResourceLocation texture, int xPos, int yPos, int width, int height) {
    // we draw full height row-wise
    int full = height / this.h;
    for (int i = 0; i < full; i++) {
      this.drawScaledX(guiGraphics, texture, xPos, yPos + i * this.h, width);
    }

    yPos += full * this.h;

    // and the remainder is drawn manually
    int yRest = height % this.h;
    // the same as drawScaledX but with the remaining height
    for (int i = 0; i < width / this.w; i++) {
      this.drawScaledY(guiGraphics, texture, xPos + i * this.w, yPos, yRest);
    }

    // remainder that doesn't fit total width
    int remainder = width % this.w;

    if (remainder > 0) {
      guiGraphics.blit(texture, xPos + width - remainder, yPos, this.x, this.y, remainder, yRest, this.texW, this.texH);
    }

    return width;
  }

  @Override
  public ScalableElementScreen shift(int xd, int yd) {
    ScalableElementScreen element = new ScalableElementScreen(this.x + xd, this.y + yd, this.w, this.h);
    element.setTextureSize(this.texW, this.texH);
    return element;
  }
}
