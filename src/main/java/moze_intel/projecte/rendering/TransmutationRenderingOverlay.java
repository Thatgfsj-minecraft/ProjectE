package moze_intel.projecte.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import java.util.ArrayList;
import java.util.List;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.items.PhilosophersStone;
import moze_intel.projecte.gameObjs.items.PhilosophersStone.PhilosophersStoneMode;
import moze_intel.projecte.gameObjs.registries.PEItems;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.CustomBlockOutlineRenderer;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.client.gui.GuiLayer;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TransmutationRenderingOverlay implements GuiLayer {

	private record OutlineTarget(BlockPos pos, VoxelShape shape) {
	}

	private final Minecraft mc = Minecraft.getInstance();
	@Nullable
	private Block transmutationResult;
	private long lastGameTime;
	@Nullable
	private List<OutlineTarget> outlineTargets = List.of();
	private float outlineAlpha;

	public TransmutationRenderingOverlay() {
		NeoForge.EVENT_BUS.addListener(this::onBlockOutlineExtract);
	}

	@Override
	public void render(@NotNull GuiGraphics graphics, @NotNull DeltaTracker delta) {
		if (transmutationResult != null) {
			//Just render it normally instead of with the given model as some block's don't render properly then as an item
			// for example glass panes
			graphics.renderItem(new ItemStack(transmutationResult), 1, 1);
			long gameTime = mc.level == null ? 0 : mc.level.getGameTime();
			if (lastGameTime != gameTime) {
				//If the game time changed, so we aren't actually still hovering a block set our
				// result to null. We do this after rendering it just in case there is a single
				// frame where this may actually be valid based on the order the events are fired
				transmutationResult = null;
				lastGameTime = gameTime;
			}
		}
	}

	private void onBlockOutlineExtract(ExtractBlockOutlineRenderStateEvent event) {
		Camera camera = event.getCamera();
		Entity entity = camera.entity();
		if (!(entity instanceof Player player)) {
			clearOverlay();
			return;
		}
		Level level = event.getLevel();
		lastGameTime = level.getGameTime();
		ItemStack stack = player.getMainHandItem();
		if (stack.isEmpty()) {
			stack = player.getOffhandItem();
		}
		if (stack.isEmpty() || !stack.is(PEItems.PHILOSOPHERS_STONE)) {
			clearOverlay();
			return;
		}
		boolean isSneaking = player.isSecondaryUseActive();
		PhilosophersStone philoStone = (PhilosophersStone) stack.getItem();
		//Note: We use the philo stone's ray trace instead of the event's ray trace as we want to make sure that we
		// can properly take fluid into account/ignore it when needed
		BlockHitResult rtr = philoStone.getHitBlock(level, player, isSneaking);
		if (rtr.getType() != HitResult.Type.BLOCK) {
			clearOverlay();
			return;
		}
		int charge = philoStone.getCharge(stack);
		PhilosophersStoneMode mode = philoStone.getMode(stack);
		Object2ReferenceMap<BlockPos, BlockState> changes = PhilosophersStone.getChanges(level, rtr.getBlockPos(), rtr.getDirection(), player.getDirection(),
				isSneaking, mode, charge);
		if (changes.isEmpty()) {
			clearOverlay();
			return;
		}
		transmutationResult = changes.values().iterator().next().getBlock();
		outlineAlpha = ProjectEConfig.client.pulsatingOverlay.get() ? getPulseProportion() * 0.60F : 0.35F;
		CollisionContext selectionContext = event.getCollisionContext();
		List<OutlineTarget> targets = new ArrayList<>(changes.size());
		for (BlockPos pos : changes.keySet()) {
			BlockState state = level.getBlockState(pos);
			if (!state.isAir()) {
				VoxelShape shape = state.getShape(level, pos, selectionContext);
				if (!shape.isEmpty()) {
					targets.add(new OutlineTarget(pos.immutable(), shape));
				}
			}
		}
		outlineTargets = List.copyOf(targets);
		if (!outlineTargets.isEmpty()) {
			event.addCustomRenderer(OUTLINE_RENDERER);
		}
	}

	private void clearOverlay() {
		transmutationResult = null;
		outlineTargets = List.of();
	}

	private final CustomBlockOutlineRenderer OUTLINE_RENDERER = this::renderTransmutationOutlines;

	private boolean renderTransmutationOutlines(BlockOutlineRenderState renderState, MultiBufferSource.BufferSource bufferSource, PoseStack poseStack,
			boolean translucentPass, LevelRenderState levelRenderState) {
		//1.21.11 每帧会分别在不透明/半透明两个 pass 调用一次 outline 渲染器，只画一次避免双重混合
		if (translucentPass) {
			return false;
		}
		Vec3 camera = levelRenderState.cameraRenderState.pos;
		int color = ((int) (outlineAlpha * 255) << 24) | 0xFFFFFF;
		RenderType renderType = PERenderType.TRANSMUTATION_OVERLAY;
		VertexConsumer buffer = bufferSource.getBuffer(renderType);
		for (OutlineTarget target : outlineTargets) {
			poseStack.pushPose();
			//Shift by view position here so that we don't have floating point issues at large values
			poseStack.translate(target.pos().getX() - camera.x, target.pos().getY() - camera.y, target.pos().getZ() - camera.z);
			ShapeRenderer.renderShape(poseStack, buffer, target.shape(), 0, 0, 0, color, 2.0F);
			poseStack.popPose();
		}
		bufferSource.endBatch(renderType);
		return !outlineTargets.isEmpty();
	}

	private float getPulseProportion() {
		return (float) (0.5F * Math.sin(System.currentTimeMillis() / 350.0) + 0.5F);
	}
}
