package moze_intel.projecte.emc.components.processor;

import java.util.Optional;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.components.DataComponentProcessor;
import moze_intel.projecte.api.proxy.IEMCProxy;
import moze_intel.projecte.config.PEConfigTranslations;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

@DataComponentProcessor
public class ArmorTrimProcessor extends PersistentComponentProcessor<ArmorTrim> {

	@Override
	public String getName() {
		return PEConfigTranslations.DCP_ARMOR_TRIM.title();
	}

	@Override
	public String getTranslationKey() {
		return PEConfigTranslations.DCP_ARMOR_TRIM.getTranslationKey();
	}

	@Override
	public String getDescription() {
		return PEConfigTranslations.DCP_ARMOR_TRIM.tooltip();
	}

	@Override
	@Range(from = 0, to = Long.MAX_VALUE)
	public long recalculateEMC(@NotNull ItemInfo info, @Range(from = 1, to = Long.MAX_VALUE) long currentEMC, @NotNull ArmorTrim trim) throws ArithmeticException {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (server == null) {
			return 0;
		}
		//Note: 1.21.11 no longer stores the material/template item on TrimMaterial/TrimPattern, instead the smithing trim recipes
		// define which items represent them (the addition provides the trim material via the PROVIDES_TRIM_MATERIAL component).
		long cheapest = Long.MAX_VALUE;
		ItemStack base = info.createStack();
		base.remove(DataComponents.TRIM);
		for (RecipeHolder<?> holder : server.getRecipeManager().recipeMap().byType(RecipeType.SMITHING)) {
			if (!(holder.value() instanceof SmithingTrimRecipe recipe)) {
				continue;
			}
			for (ItemStack materialStack : matchingStacks(recipe.addition)) {
				Optional<Holder<TrimMaterial>> material = TrimMaterials.getFromIngredient(server.registryAccess(), materialStack);
				if (material.isEmpty() || !trim.material().equals(material.get())) {
					continue;
				}
				long materialEmc = IEMCProxy.INSTANCE.getValue(materialStack);
				if (materialEmc == 0) {
					continue;
				}
				for (ItemStack templateStack : matchingStacks(recipe.template)) {
					long templateEmc = IEMCProxy.INSTANCE.getValue(templateStack);
					if (templateEmc == 0) {
						continue;
					}
					ItemStack result = recipe.assemble(new SmithingRecipeInput(templateStack, base, materialStack), server.registryAccess());
					if (trim.equals(result.get(DataComponents.TRIM))) {
						cheapest = Math.min(cheapest, Math.addExact(materialEmc, templateEmc));
					}
				}
			}
		}
		return cheapest == Long.MAX_VALUE ? 0 : Math.addExact(currentEMC, cheapest);
	}

	private static Iterable<ItemStack> matchingStacks(@NotNull Ingredient ingredient) {
		return ingredient.items().map(holder -> new ItemStack(holder.value())).toList();
	}

	@Override
	protected boolean validItem(@NotNull ItemInfo info) {
		return info.getItem().is(ItemTags.TRIMMABLE_ARMOR);
	}

	@Override
	protected boolean shouldPersist(@NotNull ItemInfo info, @NotNull ArmorTrim component) {
		return true;
	}

	@Override
	protected DataComponentType<ArmorTrim> getComponentType(@NotNull ItemInfo info) {
		return DataComponents.TRIM;
	}
}
