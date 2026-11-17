package moze_intel.projecte.utils;

import moze_intel.projecte.api.inventory.PEItemStacksHandler;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;

/**
 * A fixed-size view over a contiguous slice of a {@link PEItemStacksHandler} that also supports direct slot
 * mutation. NeoForge's {@code RangedResourceHandler} is a read/write view without {@link IndexModifier},
 * which breaks compaction (it needs to reassign slot contents).
 */
public class SlicedItemStacksHandler implements ResourceHandler<ItemResource>, IndexModifier<ItemResource> {

	private final PEItemStacksHandler delegate;
	private final int start;
	private final int size;

	public SlicedItemStacksHandler(@NotNull PEItemStacksHandler delegate, int start, int size) {
		this.delegate = delegate;
		this.start = start;
		this.size = size;
	}

	@NotNull
	public PEItemStacksHandler getDelegate() {
		return delegate;
	}

	private int translate(int index) {
		return start + index;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public ItemResource getResource(int index) {
		return delegate.getResource(translate(index));
	}

	@Override
	public long getAmountAsLong(int index) {
		return delegate.getAmountAsLong(translate(index));
	}

	@Override
	public long getCapacityAsLong(int index, ItemResource resource) {
		return delegate.getCapacityAsLong(translate(index), resource);
	}

	@Override
	public boolean isValid(int index, ItemResource resource) {
		return delegate.isValid(translate(index), resource);
	}

	@Override
	public int insert(int index, ItemResource resource, int amount, @NotNull TransactionContext transaction) {
		return delegate.insert(translate(index), resource, amount, transaction);
	}

	@Override
	public int extract(int index, ItemResource resource, int amount, @NotNull TransactionContext transaction) {
		return delegate.extract(translate(index), resource, amount, transaction);
	}

	@Override
	public void set(int index, ItemResource resource, int amount) {
		delegate.set(translate(index), resource, amount);
	}
}