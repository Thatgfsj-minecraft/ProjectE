package moze_intel.projecte.gameObjs.container.slots;

import moze_intel.projecte.api.inventory.PEItemStacksHandler;
import moze_intel.projecte.utils.PECombinedItemStacks;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Slot backed by a {@link ResourceHandler}.
 * <p>
 * This is a local copy of NeoForge's {@code StackCopySlot} + {@link ResourceHandlerSlot} (21.11.48) because {@code StackCopySlot#remove(int)} is
 * {@code final} in 21.11.48, while ProjectE's slots (output/input/lock slots) need to override it. The 26.3 upstream code could simply inherit it.
 */
public class InventoryContainerSlot extends Slot implements IInventoryContainerSlot {

	private static final Container EMPTY_INVENTORY = new SimpleContainer(0);

	private final ResourceHandler<ItemResource> handler;
	private final IndexModifier<ItemResource> slotModifier;
	private final int index;
	@Nullable
	private ItemStack cachedReturnedStack = null;

	public InventoryContainerSlot(ResourceHandler<ItemResource> itemHandler, int index, int x, int y) {
		this(itemHandler, PEItemStacksHandler.requireModifier(itemHandler), index, x, y);
	}

	public InventoryContainerSlot(PEItemStacksHandler itemHandler, int index, int x, int y) {
		this(itemHandler, itemHandler, index, x, y);
	}

	public InventoryContainerSlot(PECombinedItemStacks itemHandler, int index, int x, int y) {
		this(itemHandler, itemHandler, index, x, y);
	}

	public InventoryContainerSlot(ResourceHandler<ItemResource> handler, IndexModifier<ItemResource> modifier, int index, int x, int y) {
		super(EMPTY_INVENTORY, 0, x, y);
		this.handler = handler;
		this.slotModifier = modifier;
		this.index = index;
	}

	public ResourceHandler<ItemResource> getResourceHandler() {
		return handler;
	}

	@Override
	public int getSlotIndex() {
		//1.21.11's StackCopySlot/ResourceHandlerSlot passes 0 to Slot's constructor (the backing Container is a dummy),
		// which would make getSlotIndex() return 0 for every slot. ProjectE's transmutation slots use it to sync the right slot,
		// so expose the real handler index (that is what SlotItemHandler did in 1.21.1).
		return index;
	}

	@Override
	public int getContainerSlot() {
		return index;
	}

	@NotNull
	@Override
	public ItemStack getItem() {
		return cachedReturnedStack = getStackCopy();
	}

	@Override
	public void set(@NotNull ItemStack stack) {
		setStackCopy(stack);
		cachedReturnedStack = stack;
	}

	@Override
	public void setChanged() {
		// Verify that the stack has actually changed before setting it.
		if (cachedReturnedStack != null && !ItemStack.matches(cachedReturnedStack, getStackCopy())) {
			set(cachedReturnedStack);
		}
	}

	@NotNull
	@Override
	public ItemStack remove(int amount) {
		ItemStack stack = getStackCopy().copy();
		ItemStack ret = stack.split(amount);
		set(stack);
		cachedReturnedStack = null;
		return ret;
	}

	protected ItemStack getStackCopy() {
		return handler.getResource(index).toStack(handler.getAmountAsInt(index));
	}

	protected void setStackCopy(@NotNull ItemStack stack) {
		slotModifier.set(index, ItemResource.of(stack), stack.getCount());
	}

	@Override
	public boolean mayPlace(@NotNull ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		// Use isValid as a reasonable estimate.
		return handler.isValid(index, ItemResource.of(stack));
	}

	@Override
	public void onQuickCraft(@NotNull ItemStack oldStackIn, @NotNull ItemStack newStackIn) {
	}

	@Override
	public int getMaxStackSize() {
		return handler.getCapacityAsInt(index, ItemResource.EMPTY);
	}

	@Override
	public int getMaxStackSize(@NotNull ItemStack stack) {
		return Math.min(getMaxStackSize(), stack.getMaxStackSize());
	}

	@Override
	public boolean mayPickup(@NotNull Player player) {
		ItemResource resource = handler.getResource(index);
		if (resource.isEmpty()) {
			return false;
		}
		try (Transaction tx = Transaction.openRoot()) {
			//Simulated extraction
			return handler.extract(index, resource, 1, tx) == 1;
		}
	}

	@Override
	public boolean isSameInventory(@NotNull Slot other) {
		if (other instanceof InventoryContainerSlot slot) {
			return slot.handler == this.handler;
		}
		return other instanceof ResourceHandlerSlot slot && slot.getResourceHandler() == this.handler;
	}
}
