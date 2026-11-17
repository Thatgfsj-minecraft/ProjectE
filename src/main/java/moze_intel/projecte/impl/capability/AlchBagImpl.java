package moze_intel.projecte.impl.capability;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.capabilities.IAlchBagProvider;
import moze_intel.projecte.gameObjs.registries.PEAttachmentTypes;
import moze_intel.projecte.impl.codec.PECodecHelper;
import moze_intel.projecte.network.PEStreamCodecs;
import moze_intel.projecte.network.packets.to_client.alch_bag.SyncAllBagDataPKT;
import moze_intel.projecte.network.packets.to_client.alch_bag.SyncBagsDataPKT;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import moze_intel.projecte.api.inventory.PEItemStacksHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AlchBagImpl implements IAlchBagProvider {

	private final Player player;

	public AlchBagImpl(Player player) {
		this.player = player;
	}

	private AlchemicalBagAttachment attachment() {
		return this.player.getData(PEAttachmentTypes.ALCHEMICAL_BAGS);
	}

	@NotNull
	@Override
	public PEItemStacksHandler getBag(@NotNull DyeColor color) {
		return attachment().getBag(color);
	}

	@Override
	public void sync(@NotNull ServerPlayer player, @NotNull Set<DyeColor> colors) {
		if (!colors.isEmpty()) {
			AlchemicalBagAttachment attachment = attachment();
			Map<DyeColor, PEItemStacksHandler> handlers = new EnumMap<>(DyeColor.class);
			for (DyeColor color : colors) {
				handlers.put(color, attachment.getBag(color));
			}
			PacketDistributor.sendToPlayer(player, new SyncBagsDataPKT(handlers));
		}
	}

	@Override
	public void syncAllBags(@NotNull ServerPlayer player) {
		PacketDistributor.sendToPlayer(player, new SyncAllBagDataPKT(attachment()));
	}

	public static class AlchemicalBagAttachment {

		private static final int BAG_SIZE = 104;
		private static final Codec<Map<DyeColor, PEItemStacksHandler>> LEGACY_INVENTORY_CODEC = Codec.unboundedMap(DyeColor.CODEC, PECodecHelper.MUTABLE_HANDLER_CODEC);
		public static final MapCodec<AlchemicalBagAttachment> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				LEGACY_INVENTORY_CODEC.fieldOf("inventories")
						.forGetter(attachment -> attachment.inventories)
		).apply(instance, map -> new AlchemicalBagAttachment(map.isEmpty() ? new EnumMap<>(DyeColor.class) : new EnumMap<>(map))));
		/**
		 * Parses both the legacy 1.21.1 layout (a bare dye-to-inventory map) and the current layout
		 * ({"inventories": {...}}). Used by the offline bag reader and as the engine for {@link #SERIALIZER}.
		 */
		public static final Codec<AlchemicalBagAttachment> CODEC = Codec.either(LEGACY_INVENTORY_CODEC, MAP_CODEC.codec()).xmap(
				either -> either.map(AlchemicalBagAttachment::new, Function.identity()),
				attachment -> Either.left(attachment.inventories));
		/**
		 * NeoForge 21.11 only accepts a MapCodec for attachment persistence, which stores the inventories under an extra
		 * "inventories" field. The 1.21.1 layout was a bare dye-to-inventory map, so this serializer reads both layouts and
		 * keeps writing the legacy one to stay compatible with existing saves.
		 */
		public static final IAttachmentSerializer<AlchemicalBagAttachment> SERIALIZER = new IAttachmentSerializer<>() {
			@NotNull
			@Override
			public AlchemicalBagAttachment read(IAttachmentHolder holder, @NotNull ValueInput input) {
				Optional<AlchemicalBagAttachment> current = input.read(MAP_CODEC);
				if (current.isPresent()) {
					return current.get();
				}
				Map<DyeColor, PEItemStacksHandler> legacy = new EnumMap<>(DyeColor.class);
				for (DyeColor color : DyeColor.values()) {
					input.read(color.getName(), PECodecHelper.MUTABLE_HANDLER_CODEC).ifPresent(handler -> legacy.put(color, handler));
				}
				return new AlchemicalBagAttachment(legacy);
			}

			@Override
			public boolean write(AlchemicalBagAttachment attachment, @NotNull ValueOutput output) {
				for (Map.Entry<DyeColor, PEItemStacksHandler> entry : attachment.inventories.entrySet()) {
					output.store(entry.getKey().getName(), PECodecHelper.MUTABLE_HANDLER_CODEC, entry.getValue());
				}
				return true;
			}
		};

		public static final StreamCodec<RegistryFriendlyByteBuf, Map<DyeColor, PEItemStacksHandler>> MAP_STREAM_CODEC = ByteBufCodecs.map(
				ignored -> new EnumMap<>(DyeColor.class),
				DyeColor.STREAM_CODEC,
				PEStreamCodecs.handlerStreamCodec(BAG_SIZE)
		);
		public static final StreamCodec<RegistryFriendlyByteBuf, AlchemicalBagAttachment> STREAM_CODEC = MAP_STREAM_CODEC.map(
				AlchemicalBagAttachment::new, attachment -> attachment.inventories
		);

		private final Map<DyeColor, PEItemStacksHandler> inventories;

		public AlchemicalBagAttachment(@Nullable IAttachmentHolder unused) {
			this(new EnumMap<>(DyeColor.class));
		}

		private AlchemicalBagAttachment(Map<DyeColor, PEItemStacksHandler> inventories) {
			this.inventories = inventories;
		}

		@Nullable
		public AlchemicalBagAttachment copy(IAttachmentHolder holder, HolderLookup.Provider registries) {
			AlchemicalBagAttachment copy = new AlchemicalBagAttachment(holder);
			for (Map.Entry<DyeColor, PEItemStacksHandler> entry : inventories.entrySet()) {
				copy.inventories.put(entry.getKey(), PEAttachmentTypes.copyHandler(entry.getValue(), PEItemStacksHandler::new));
			}
			return copy;
		}

		@NotNull
		public PEItemStacksHandler getBag(@NotNull DyeColor color) {
			return inventories.computeIfAbsent(color, c -> new PEItemStacksHandler(BAG_SIZE));
		}

		public void updateBags(Map<DyeColor, PEItemStacksHandler> handlers) {
			for (Map.Entry<DyeColor, PEItemStacksHandler> entry : handlers.entrySet()) {
				DyeColor color = entry.getKey();
				PEItemStacksHandler handler = entry.getValue();
				if (handler.getSlots() == BAG_SIZE) {
					inventories.put(color, handler);
				} else {
					PECore.LOGGER.warn("Received packet for updating {}, but the handler was of the wrong size. Expected: {}, Received: {}", color, BAG_SIZE, handler.getSlots());
				}
			}
		}
	}
}