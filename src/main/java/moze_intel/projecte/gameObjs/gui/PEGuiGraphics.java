package moze_intel.projecte.gameObjs.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public final class PEGuiGraphics {

	private static final int DEFAULT_TEXTURE_SIZE = 256;
	/** 1.21.6+ 要求 alpha 非零，{@code 0x404040} 会完全透明。 */
	public static final int LABEL_COLOR = 0xFF404040;

	private PEGuiGraphics() {
	}

	public static void blit(@NotNull GuiGraphics graphics, @NotNull Identifier texture, int x, int y, int u, int v, int width, int height) {
		graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, DEFAULT_TEXTURE_SIZE, DEFAULT_TEXTURE_SIZE);
	}

	public static void blit(@NotNull GuiGraphics graphics, @NotNull Identifier texture, int x, int y, int u, int v, int width, int height,
			int textureWidth, int textureHeight) {
		graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, textureWidth, textureHeight);
	}
}
