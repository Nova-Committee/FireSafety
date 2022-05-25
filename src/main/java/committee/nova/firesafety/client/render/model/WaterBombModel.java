package committee.nova.firesafety.client.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import committee.nova.firesafety.FireSafety;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class WaterBombModel<T extends Entity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(FireSafety.MODID, "textures/entity/bombs/water_bomb/water_bomb.png"), "main");
    private final ModelPart waterBomb;

    public WaterBombModel(ModelPart root) {
        this.waterBomb = root.getChild("main");
    }

    public static LayerDefinition createBodyLayer() {
        final var meshDefinition = new MeshDefinition();
        final var partDefinition = meshDefinition.getRoot();
        final var main = partDefinition.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 23).addBox(-3.0F, -6.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-6.0F, -21.0F, -4.0F, 12.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));
        main.addOrReplaceChild("s4_r1", CubeListBuilder.create().texOffs(24, 23).addBox(-1.0F, -21.0F, 0.0F, 1.0F, 15.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2182F, 0.0F, -0.2182F));
        main.addOrReplaceChild("s3_r1", CubeListBuilder.create().texOffs(28, 23).addBox(-1.0F, -21.0F, -1.0F, 1.0F, 15.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2182F, 0.0F, -0.2182F));
        main.addOrReplaceChild("s2_r1", CubeListBuilder.create().texOffs(32, 23).addBox(0.0F, -21.0F, -1.0F, 1.0F, 15.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2182F, 0.0F, 0.2182F));
        main.addOrReplaceChild("s1_r1", CubeListBuilder.create().texOffs(0, 35).addBox(0.0F, -21.0F, 0.0F, 1.0F, 15.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2182F, 0.0F, 0.2182F));
        main.addOrReplaceChild("um1_r1", CubeListBuilder.create().texOffs(0, 16).addBox(-6.0F, -13.6F, -0.1F, 12.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.0F, 0.0F, -0.3054F, 0.0F, 0.0F));
        main.addOrReplaceChild("um1_r2", CubeListBuilder.create().texOffs(0, 9).addBox(-6.0F, -13.6F, -5.9F, 12.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.0F, 0.0F, 0.3054F, 0.0F, 0.0F));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        waterBomb.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}