package moze_intel.projecte.common.datagen;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Helpers for looking up the members of the vanilla colored item families by {@link DyeColor}.
 * <p>
 * Newer Minecraft versions expose {@code ColorCollection} constants that can be queried in bulk, but 1.21.11 does not have that API, so
 * every family is mapped explicitly instead.
 */
public final class ColorCollections {

	private ColorCollections() {
	}

	public static Item woolItem(DyeColor color) {
		return switch (color) {
			case WHITE -> Items.WHITE_WOOL;
			case ORANGE -> Items.ORANGE_WOOL;
			case MAGENTA -> Items.MAGENTA_WOOL;
			case LIGHT_BLUE -> Items.LIGHT_BLUE_WOOL;
			case YELLOW -> Items.YELLOW_WOOL;
			case LIME -> Items.LIME_WOOL;
			case PINK -> Items.PINK_WOOL;
			case GRAY -> Items.GRAY_WOOL;
			case LIGHT_GRAY -> Items.LIGHT_GRAY_WOOL;
			case CYAN -> Items.CYAN_WOOL;
			case PURPLE -> Items.PURPLE_WOOL;
			case BLUE -> Items.BLUE_WOOL;
			case BROWN -> Items.BROWN_WOOL;
			case GREEN -> Items.GREEN_WOOL;
			case RED -> Items.RED_WOOL;
			case BLACK -> Items.BLACK_WOOL;
		};
	}

	public static Item concreteItem(DyeColor color) {
		return switch (color) {
			case WHITE -> Items.WHITE_CONCRETE;
			case ORANGE -> Items.ORANGE_CONCRETE;
			case MAGENTA -> Items.MAGENTA_CONCRETE;
			case LIGHT_BLUE -> Items.LIGHT_BLUE_CONCRETE;
			case YELLOW -> Items.YELLOW_CONCRETE;
			case LIME -> Items.LIME_CONCRETE;
			case PINK -> Items.PINK_CONCRETE;
			case GRAY -> Items.GRAY_CONCRETE;
			case LIGHT_GRAY -> Items.LIGHT_GRAY_CONCRETE;
			case CYAN -> Items.CYAN_CONCRETE;
			case PURPLE -> Items.PURPLE_CONCRETE;
			case BLUE -> Items.BLUE_CONCRETE;
			case BROWN -> Items.BROWN_CONCRETE;
			case GREEN -> Items.GREEN_CONCRETE;
			case RED -> Items.RED_CONCRETE;
			case BLACK -> Items.BLACK_CONCRETE;
		};
	}

	public static Item concretePowderItem(DyeColor color) {
		return switch (color) {
			case WHITE -> Items.WHITE_CONCRETE_POWDER;
			case ORANGE -> Items.ORANGE_CONCRETE_POWDER;
			case MAGENTA -> Items.MAGENTA_CONCRETE_POWDER;
			case LIGHT_BLUE -> Items.LIGHT_BLUE_CONCRETE_POWDER;
			case YELLOW -> Items.YELLOW_CONCRETE_POWDER;
			case LIME -> Items.LIME_CONCRETE_POWDER;
			case PINK -> Items.PINK_CONCRETE_POWDER;
			case GRAY -> Items.GRAY_CONCRETE_POWDER;
			case LIGHT_GRAY -> Items.LIGHT_GRAY_CONCRETE_POWDER;
			case CYAN -> Items.CYAN_CONCRETE_POWDER;
			case PURPLE -> Items.PURPLE_CONCRETE_POWDER;
			case BLUE -> Items.BLUE_CONCRETE_POWDER;
			case BROWN -> Items.BROWN_CONCRETE_POWDER;
			case GREEN -> Items.GREEN_CONCRETE_POWDER;
			case RED -> Items.RED_CONCRETE_POWDER;
			case BLACK -> Items.BLACK_CONCRETE_POWDER;
		};
	}
}
