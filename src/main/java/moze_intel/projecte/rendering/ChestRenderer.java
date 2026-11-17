package moze_intel.projecte.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import moze_intel.projecte.gameObjs.block_entities.EmcChestBlockEntity;
import moze_intel.projecte.gameObjs.registration.impl.BlockRegistryObject;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.chest.ChestModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

//Only used on the client
// [VanillaCopy] Adapted from ChestRenderer
public class ChestRenderer implements BlockEntityRenderer<EmcChestBlockEntity, ChestRenderState> {

	private final ChestModel model;
	private final Identifier texture;

	public ChestRenderer(BlockEntityRendererProvider.Context context, Identifier texture, BlockRegistryObject<?, ?> type) {
		this.model = new ChestModel(context.bakeLayer(ModelLayers.CHEST));
		this.texture = texture;
	}

	@Override
	public ChestRenderState createRenderState() {
		return new ChestRenderState();
	}

	@Override
	public void extractRenderState(EmcChestBlockEntity chest, ChestRenderState state, float partialTick, Vec3 cameraPosition,
			ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(chest, state, partialTick, cameraPosition, breakProgress);
		state.open = chest.getOpenNess(partialTick);
		state.facing = chest.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
	}

	@Override
	public void submit(ChestRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		poseStack.pushPose();
		poseStack.translate(0.5F, 0.5F, 0.5F);
		poseStack.mulPose(Axis.YP.rotationDegrees(-state.facing.toYRot()));
		poseStack.translate(-0.5F, -0.5F, -0.5F);
		float closed = 1.0F - state.open;
		collector.submitModel(model, 1.0F - closed * closed * closed, poseStack, RenderTypes.entityCutout(this.texture),
				state.lightCoords, OverlayTexture.NO_OVERLAY, -1, state.breakProgress);
		poseStack.popPose();
	}
}
