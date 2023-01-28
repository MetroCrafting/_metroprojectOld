package shadersmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.src.GlStateManager;
import net.minecraft.util.MathHelper;

public class GuiSliderShaderOption extends GuiButtonShaderOption {
      private float sliderValue = 1.0F;
      public boolean dragging;
      private ShaderOption shaderOption = null;

      public GuiSliderShaderOption(int buttonId, int x, int y, int w, int h, ShaderOption shaderOption, String text) {
            super(buttonId, x, y, w, h, shaderOption, text);
            this.shaderOption = shaderOption;
            this.sliderValue = shaderOption.getIndexNormalized();
            super.displayString = GuiShaderOptions.getButtonText(shaderOption, this.width);
      }

      public int getHoverState(boolean mouseOver) {
            return 0;
      }

      protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
            if (this.visible) {
                  if (this.dragging) {
                        this.sliderValue = (float)(mouseX - (this.xPosition + 4)) / (float)(this.width - 8);
                        this.sliderValue = MathHelper.clamp_float(this.sliderValue, 0.0F, 1.0F);
                        this.shaderOption.setIndexNormalized(this.sliderValue);
                        this.sliderValue = this.shaderOption.getIndexNormalized();
                        super.displayString = GuiShaderOptions.getButtonText(this.shaderOption, this.width);
                  }

                  mc.getTextureManager().bindTexture(GuiButton.buttonTextures);
                  GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                  this.drawTexturedModalRect(this.xPosition + (int)(this.sliderValue * (float)(this.width - 8)), this.yPosition, 0, 66, 4, 20);
                  this.drawTexturedModalRect(this.xPosition + (int)(this.sliderValue * (float)(this.width - 8)) + 4, this.yPosition, 196, 66, 4, 20);
            }

      }

      public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
            if (super.mousePressed(mc, mouseX, mouseY)) {
                  this.sliderValue = (float)(mouseX - (this.xPosition + 4)) / (float)(this.width - 8);
                  this.sliderValue = MathHelper.clamp_float(this.sliderValue, 0.0F, 1.0F);
                  this.shaderOption.setIndexNormalized(this.sliderValue);
                  super.displayString = GuiShaderOptions.getButtonText(this.shaderOption, this.width);
                  this.dragging = true;
                  return true;
            } else {
                  return false;
            }
      }

      public void mouseReleased(int mouseX, int mouseY) {
            this.dragging = false;
      }

      public void valueChanged() {
            this.sliderValue = this.shaderOption.getIndexNormalized();
      }
}
