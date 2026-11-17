package moze_intel.projecte.gameObjs.gui;

import java.math.BigInteger;
import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.container.TransmutationContainer;
import moze_intel.projecte.gameObjs.container.inventory.TransmutationInventory;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.TransmutationEMCFormatter;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

public class GUITransmutation extends PEContainerScreen<TransmutationContainer> {

	private static final BigInteger MAX_EXACT_TRANSMUTATION_DISPLAY = BigInteger.valueOf(1_000_000_000_000L);
	private static final Identifier texture = PECore.rl("textures/gui/transmute.png");

	private final TransmutationInventory inv;
	private EditBox textBoxFilter;
	private Button previous, next;

	public GUITransmutation(TransmutationContainer container, Inventory invPlayer, Component title) {
		super(container, invPlayer, title, 228, 196);
		this.inv = container.transmutationInventory;
		this.titleLabelX = 6;
		this.titleLabelY = 8;
	}

	@Override
	public void init() {
		super.init();

		this.textBoxFilter = addWidget(new EditBox(this.font, leftPos + 83, topPos + 8, 55, 10, Component.empty()));
		this.textBoxFilter.setResponder(inv::updateFilter);

		//Note: We don't have to change the filter when changing pages as the filter should already be set
		previous = addRenderableWidget(Button.builder(Component.literal("<"), b -> inv.previousPage())
				.pos(leftPos + 125, topPos + 100)
				.size(14, 14)
				.build());
		next = addRenderableWidget(Button.builder(Component.literal(">"), b -> inv.nextPage())
				.pos(leftPos + 193, topPos + 100)
				.size(14, 14)
				.build());
		updateButtons();
	}

	@Override
	public void resize(int width, int height) {
		String filter = this.textBoxFilter == null ? "" : this.textBoxFilter.getValue();
		super.resize(width, height);
		if (this.textBoxFilter != null) {
			this.textBoxFilter.setValue(filter);
		}
	}

	@Override
	protected void containerTick() {
		super.containerTick();
		updateButtons();
	}

	private void updateButtons() {
		if (previous != null) {
			previous.active = inv.hasPreviousPage();
		}
		if (next != null) {
			next.active = inv.hasNextPage();
		}
	}

	@Override
	protected void renderBg(@NotNull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
		PEGuiGraphics.blit(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);
		this.textBoxFilter.render(graphics, mouseX, mouseY, partialTicks);
	}

	@Override
	protected void renderLabels(@NotNull GuiGraphics graphics, int x, int y) {
		graphics.drawString(font, title, titleLabelX, titleLabelY, PEGuiGraphics.LABEL_COLOR, false);
		//Don't render inventory as we don't have space
		graphics.drawString(font, PELang.EMC_TOOLTIP.translate(""), 6, this.imageHeight - 104, PEGuiGraphics.LABEL_COLOR, false);
		Component emc = TransmutationEMCFormatter.formatEMC(inv.getAvailableEmc());
		graphics.drawString(font, emc, 6, this.imageHeight - 94, PEGuiGraphics.LABEL_COLOR, false);

		if (inv.learnFlag > 0) {
			graphics.drawString(font, PELang.TRANSMUTATION_LEARNED_1.translate(), 98, 30, PEGuiGraphics.LABEL_COLOR, false);
			graphics.drawString(font, PELang.TRANSMUTATION_LEARNED_2.translate(), 99, 38, PEGuiGraphics.LABEL_COLOR, false);
			graphics.drawString(font, PELang.TRANSMUTATION_LEARNED_3.translate(), 100, 46, PEGuiGraphics.LABEL_COLOR, false);
			graphics.drawString(font, PELang.TRANSMUTATION_LEARNED_4.translate(), 101, 54, PEGuiGraphics.LABEL_COLOR, false);
			graphics.drawString(font, PELang.TRANSMUTATION_LEARNED_5.translate(), 102, 62, PEGuiGraphics.LABEL_COLOR, false);
			graphics.drawString(font, PELang.TRANSMUTATION_LEARNED_6.translate(), 103, 70, PEGuiGraphics.LABEL_COLOR, false);
			graphics.drawString(font, PELang.TRANSMUTATION_LEARNED_7.translate(), 104, 78, PEGuiGraphics.LABEL_COLOR, false);
			graphics.drawString(font, PELang.TRANSMUTATION_LEARNED_8.translate(), 107, 86, PEGuiGraphics.LABEL_COLOR, false);

			inv.learnFlag--;
		}

		if (inv.unlearnFlag > 0) {
			graphics.drawString(font, PELang.TRANSMUTATION_UNLEARNED_1.translate(), 97, 22, PEGuiGraphics.LABEL_COLOR, false);
			graphics.drawString(font, PELang.TRANSMUTATION_UNLEARNED_2.translate(), 98, 30, PEGuiGraphics.LABEL_COLOR, false);
			graphics.drawString(font, PELang.TRANSMUTATION_UNLEARNED_3.translate(), 99, 38, PEGuiGraphics.LABEL_COLOR, false);
			graphics.drawString(font, PELang.TRANSMUTATION_UNLEARNED_4.translate(), 100, 46, PEGuiGraphics.LABEL_COLOR, false);
			graphics.drawString(font, PELang.TRANSMUTATION_UNLEARNED_5.translate(), 101, 54, PEGuiGraphics.LABEL_COLOR, false);
			graphics.drawString(font, PELang.TRANSMUTATION_UNLEARNED_6.translate(), 102, 62, PEGuiGraphics.LABEL_COLOR, false);
			graphics.drawString(font, PELang.TRANSMUTATION_UNLEARNED_7.translate(), 103, 70, PEGuiGraphics.LABEL_COLOR, false);
			graphics.drawString(font, PELang.TRANSMUTATION_UNLEARNED_8.translate(), 104, 78, PEGuiGraphics.LABEL_COLOR, false);
			graphics.drawString(font, PELang.TRANSMUTATION_UNLEARNED_9.translate(), 107, 86, PEGuiGraphics.LABEL_COLOR, false);

			inv.unlearnFlag--;
		}
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (textBoxFilter.isFocused()) {
			//Manually make it so that hitting escape when the filter is focused will exit the focus
			if (event.isEscape()) {
				textBoxFilter.setFocused(false);
				return true;
			}
			//Otherwise have it handle the key press
			//This is where key combos and deletion is handled, and where we bypass the inventory key closing the screen
			return textBoxFilter.keyPressed(event);
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		double x = event.x();
		double y = event.y();
		if (textBoxFilter.isMouseOver(x, y) && event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
			//Note: Clearing filter will be handled by the text box's responder
			this.textBoxFilter.setValue("");
			setFocused(this.textBoxFilter);
			return true;
		}
		if (super.mouseClicked(event, doubleClick)) {
			return true;
		}
		if (textBoxFilter.isFocused()) {
			if (hoveredSlot == null || (!hoveredSlot.hasItem() && menu.getCarried().isEmpty())) {
				textBoxFilter.setFocused(false);
			}
		}
		return false;
	}

	@Override
	public void removed() {
		super.removed();
		inv.learnFlag = 0;
		inv.unlearnFlag = 0;
	}

	@Override
	protected void renderTooltip(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
		BigInteger emcAmount = inv.getAvailableEmc();

		if (emcAmount.compareTo(MAX_EXACT_TRANSMUTATION_DISPLAY) < 0) {
			super.renderTooltip(graphics, mouseX, mouseY);
			return;
		}

		int emcLeft = leftPos;
		int emcRight = emcLeft + 82;
		int emcTop = 95 + topPos;
		int emcBottom = emcTop + 15;

		if (mouseX > emcLeft && mouseX < emcRight && mouseY > emcTop && mouseY < emcBottom) {
			graphics.setTooltipForNextFrame(font, PELang.EMC_TOOLTIP.translate(EMCHelper.formatEmc(emcAmount)), mouseX, mouseY);
		} else {
			super.renderTooltip(graphics, mouseX, mouseY);
		}
	}
}
