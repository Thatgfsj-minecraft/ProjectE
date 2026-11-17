package moze_intel.projecte;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import moze_intel.projecte.client.PEModelProvider;
import moze_intel.projecte.client.PESpriteSourceProvider;
import moze_intel.projecte.client.lang.PELangProvider;
import moze_intel.projecte.client.sound.PESoundProvider;
import moze_intel.projecte.common.PEAdvancementsGenerator;
import moze_intel.projecte.common.PECustomConversionProvider;
import moze_intel.projecte.common.PEDataMapsProvider;
import moze_intel.projecte.common.PEWorldTransmutationProvider;
import moze_intel.projecte.common.loot.PEBlockLootTable;
import moze_intel.projecte.common.recipe.PERecipeProvider;
import moze_intel.projecte.common.tag.PEBlockEntityTypeTagsProvider;
import moze_intel.projecte.common.tag.PEBlockTagsProvider;
import moze_intel.projecte.common.tag.PEDamageTypeTagsProvider;
import moze_intel.projecte.common.tag.PEEntityTypeTagsProvider;
import moze_intel.projecte.common.tag.PEItemTagsProvider;
import moze_intel.projecte.common.tag.PEPotionsTagsProvider;
import moze_intel.projecte.emc.EMCMappingHandler;
import moze_intel.projecte.gameObjs.registries.PEDamageTypes;
import moze_intel.projecte.integration.IntegrationHelper;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = PECore.MODID)
public class ProjectEDataGenerator {

	/**
	 * 数据包注册表条目。1.21.11 通过 {@link GatherDataEvent#createDatapackRegistryObjects} 生成，
	 * 26.3 的 {@code GatherDataRegistryEntriesEvent} 在 1.21.11 不存在。
	 */
	private static final RegistrySetBuilder DATA_PACK_REGISTRIES = new RegistrySetBuilder()
			.add(Registries.DAMAGE_TYPE, context -> {
				for (var damageType : PEDamageTypes.DAMAGE_TYPES.values()) {
					context.register(damageType.key(), new DamageType(damageType.msgId(), damageType.exhaustion()));
				}
			});

	@SubscribeEvent
	public static void gatherServerData(GatherDataEvent.Server event) {
		EMCMappingHandler.loadMappers();
		//数据包注册表（伤害类型）必须先生成，这样后续 provider 的 lookup 才包含它们
		event.createDatapackRegistryObjects(DATA_PACK_REGISTRIES);
		CompletableFuture<Provider> lookupProvider = event.getLookupProvider();
		PackOutput packOutput = event.getGenerator().getPackOutput();

		event.createProvider((output, lookup) -> new PEBlockTagsProvider(output, lookupProvider));
		event.createProvider((output, lookup) -> new PEItemTagsProvider(output, lookupProvider));
		event.createProvider((output, lookup) -> new PEEntityTypeTagsProvider(output, lookupProvider));
		event.createProvider((output, lookup) -> new PEBlockEntityTypeTagsProvider(output, lookupProvider));
		event.createProvider((output, lookup) -> new PEDamageTypeTagsProvider(output, lookupProvider));
		event.createProvider((output, lookup) -> new PEPotionsTagsProvider(output, lookupProvider));
		event.createProvider((output, lookup) -> new PECustomConversionProvider(output, lookupProvider));
		event.createProvider((output, lookup) -> new PEWorldTransmutationProvider(output, lookupProvider));

		//其余生成器（放在 tag 之后，以便引用已生成的 tag/注册表条目）
		event.addProvider(new AdvancementProvider(packOutput, lookupProvider, List.of(new PEAdvancementsGenerator())));
		event.addProvider(new LootTableProvider(packOutput, Set.of(), List.of(
				new LootTableProvider.SubProviderEntry(PEBlockLootTable::new, LootContextParamSets.BLOCK)), lookupProvider));
		event.addProvider(new PERecipeProvider.Runner(packOutput, lookupProvider));
		//data maps（1.21.11 必须显式注册，否则 data/neoforge/data_maps/... 会残留旧文件）
		event.createProvider((o, lookup) -> new PEDataMapsProvider(o, lookupProvider));
	}

	@SubscribeEvent
	public static void gatherClientData(GatherDataEvent.Client event) {
		EMCMappingHandler.loadMappers();
		CompletableFuture<Provider> lookupProvider = event.getLookupProvider();

		event.createProvider(PELangProvider::new);
		event.createProvider(PESoundProvider::new);
		event.createProvider(PEModelProvider::new);
		event.createProvider((output, lookup) -> new PESpriteSourceProvider(output, lookupProvider));

		if (ModList.get().isLoaded(IntegrationHelper.EMI_MODID)) {
			registerEmiProviders(event, lookupProvider);
		}
	}

	private static void registerEmiProviders(GatherDataEvent.Client event, CompletableFuture<Provider> lookupProvider) {
		try {
			Class<?> aliasMappingClass = Class.forName("moze_intel.projecte.integration.recipe_viewer.alias.ProjectEAliasMapping");
			Class<?> aliasProvider = Class.forName("moze_intel.projecte.client.integration.emi.EmiAliasProvider");
			Class<?> emiDefaults = Class.forName("moze_intel.projecte.client.integration.emi.ProjectEEmiDefaults");
			java.util.function.Supplier<?> mappingSupplier = () -> {
				try {
					return aliasMappingClass.getDeclaredConstructor().newInstance();
				} catch (ReflectiveOperationException e) {
					throw new IllegalStateException(e);
				}
			};
			event.createProvider((output, lookup) -> {
				try {
					return (net.minecraft.data.DataProvider) aliasProvider
							.getConstructor(net.minecraft.data.PackOutput.class, CompletableFuture.class, String.class, java.util.function.Supplier.class)
							.newInstance(output, lookupProvider, PECore.MODID, mappingSupplier);
				} catch (ReflectiveOperationException e) {
					throw new IllegalStateException(e);
				}
			});
			event.createProvider((output, lookup) -> {
				try {
					return (net.minecraft.data.DataProvider) emiDefaults
							.getConstructor(net.minecraft.data.PackOutput.class, CompletableFuture.class)
							.newInstance(output, lookupProvider);
				} catch (ReflectiveOperationException e) {
					throw new IllegalStateException(e);
				}
			});
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Failed to register EMI data providers", e);
		}
	}
}