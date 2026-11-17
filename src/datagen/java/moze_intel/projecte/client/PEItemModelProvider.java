package moze_intel.projecte.client;

/**
 * 1.21.1 时代的 NeoForge {@code ItemModelProvider} 版本。
 *
 * <p>1.21.11 已删除 NeoForge 的 {@code client.model.generators.*} 与 {@code ExistingFileHelper}，
 * 且物品模型改为数据包 {@code assets/<ns>/items/<id>.json} + {@code minecraft:select}（组件属性）的
 * 数据驱动形式。ProjectE 的全部方块/物品模型现在统一由 {@link PEModelProvider}
 * （原版 {@link net.minecraft.client.data.models.ModelProvider}）生成，
 * 本类不再产生任何输出，仅作为历史占位保留（仓库约定不删除文件）。
 *
 * @deprecated 由 {@link PEModelProvider} 取代，不要再注册到 {@code GatherDataEvent}。
 */
@Deprecated(forRemoval = false)
public final class PEItemModelProvider {

	private PEItemModelProvider() {
	}
}
