package moze_intel.projecte.integration.curios;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;

/**
 * Views a legacy {@link IItemHandler} through NeoForge's transfer API.
 * <p>
 * 1.21.11 Curios still exposes {@link IItemHandler}; ProjectE's internals use the newer
 * {@link ResourceHandler} API. Operations are applied immediately (no transaction journaling);
 * the only consumer is the single transmutation tablet slot.
 */
@SuppressWarnings("removal")
public class ItemHandlerResourceAdapter implements ResourceHandler<ItemResource> {

	private final IItemHandler handler;

	public ItemHandlerResourceAdapter(IItemHandler handler) {
		this.handler = handler;
	}

	@Override
	public int size() {
		return handler.getSlots();
	}

	@Override
	public ItemResource getResource(int index) {
		return ItemResource.of(handler.getStackInSlot(index));
	}

	@Override
	public long getAmountAsLong(int index) {
		return handler.getStackInSlot(index).getCount();
	}

	@Override
	public long getCapacityAsLong(int index, ItemResource resource) {
		return handler.getSlotLimit(index);
	}

	@Override
	public boolean isValid(int index, ItemResource resource) {
		return resource.isEmpty() || handler.isItemValid(index, resource.toStack(1));
	}

	@Override
	public int insert(int index, ItemResource resource, int amount, @NotNull TransactionContext transaction) {
		if (resource.isEmpty() || amount <= 0) {
			return 0;
		}
		ItemStack remainder = handler.insertItem(index, resource.toStack(amount), false);
		return amount - remainder.getCount();
	}

	@Override
	public int extract(int index, ItemResource resource, int amount, @NotNull TransactionContext transaction) {
		if (resource.isEmpty() || amount <= 0) {
			return 0;
		}
		ItemStack inSlot = handler.getStackInSlot(index);
		if (inSlot.isEmpty() || !resource.matches(inSlot)) {
			return 0;
		}
		return handler.extractItem(index, Math.min(amount, inSlot.getCount()), false).getCount();
	}
}
