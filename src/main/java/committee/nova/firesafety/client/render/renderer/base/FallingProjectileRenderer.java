package committee.nova.firesafety.client.render.renderer.base;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import committee.nova.firesafety.client.render.model.WaterBombModel;
import committee.nova.firesafety.common.entity.projectile.impl.WaterBombProjectile;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.Projectile;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FallingProjectileRenderer<T extends Projectile> extends EntityRenderer<T> {
    private final ResourceLocation texture;
    private final EntityRendererProvider.Context ctx;

    public FallingProjectileRenderer(EntityRendererProvider.Context ctx, ResourceLocation r) {
        super(ctx);
        this.ctx = ctx;
        texture = r;
    }

    @Override
    public void render(T entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        final var vc = bufferIn.getBuffer(RenderType.entityCutout(this.getTextureLocation(entityIn)));
        matrixStackIn.pushPose();
        matrixStackIn.scale(.5F, .5F, .5F);
        matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(180));
        final var model = new WaterBombModel<WaterBombProjectile>(ctx.bakeLayer(WaterBombModel.LAYER_LOCATION));
        model.renderToBuffer(matrixStackIn, vc, packedLightIn, OverlayTexture.NO_OVERLAY, 1, 1, 1, 0.0625f);
        matrixStackIn.popPose();
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    @Override
    public ResourceLocation getTextureLocation(T t) {
        return texture;
    }

}
