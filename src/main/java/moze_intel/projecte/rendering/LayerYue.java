package moze_intel.projecte.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.UUID;
import moze_intel.projecte.PECore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.neoforged.fml.loading.FMLEnvironment;

public class LayerYue extends RenderLayer<AvatarRenderState, PlayerModel> {

	private static final UUID SIN_UUID = UUID.fromString("5f86012c-ca4b-451a-989c-8fab167af647");
	private static final UUID CLAR_UUID = UUID.fromString("e5c59746-9cf7-4940-a849-d09e1f1efc13");
	public static final Identifier HEART_LOC = PECore.rl("textures/models/heartcircle.png");
	public static final Identifier YUE_LOC = PECore.rl("textures/models/yuecircle.png");

	public LayerYue(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
		super(renderer);
	}

	@Override
	public void submit(PoseStack pose, SubmitNodeCollector collector, int light, AvatarRenderState state, float yRot, float xRot) {
		if (state.isInvisible) {
			return;
		}
		var level = Minecraft.getInstance().level;
		if (level == null) {
			return;
		}
		var player = level.getEntity(state.id);
		if (player == null) {
			return;
		}
		UUID id = player.getUUID();
		if (FMLEnvironment.isProduction() && !SIN_UUID.equals(id) && !CLAR_UUID.equals(id)) {
			return;
		}
		pose.pushPose();
		getParentModel().body.translateAndRotate(pose);
		double yShift = -0.498;
		if (state.isCrouching) {
			//Only modify where it renders if the player's pose is crouching
			pose.mulPose(Axis.XN.rotation(0.5F));
			yShift = -0.44;
		}
		pose.mulPose(Axis.ZP.rotation(Mth.PI));
		pose.scale(3, 3, 3);
		pose.translate(-0.5, yShift, -0.5);
		Identifier texture = CLAR_UUID.equals(id) ? HEART_LOC : YUE_LOC;
		collector.submitCustomGeometry(pose, PERenderType.YEU_RENDERER.apply(texture), (transform, buffer) -> {
			vertex(buffer, transform, light, 0, 0, 0, 0);
			vertex(buffer, transform, light, 0, 1, 0, 1);
			vertex(buffer, transform, light, 1, 1, 1, 1);
			vertex(buffer, transform, light, 1, 0, 1, 0);
		});
		pose.popPose();
	}

	private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, int light, float x, float z, float u, float v) {
		buffer.addVertex(pose, x, 0, z).setUv(u, v).setColor(0, 255, 0, 255)
				.setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
	}
}
