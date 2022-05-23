package committee.nova.firesafety.common.item.api;

import net.minecraft.client.model.HumanoidModel;

public interface IArmPoseChangeable {
    default HumanoidModel.ArmPose getSprintingPose() {
        return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
    }

    default HumanoidModel.ArmPose getUsingPose() {
        return HumanoidModel.ArmPose.BOW_AND_ARROW;
    }

    default HumanoidModel.ArmPose getIdlePose() {
        return HumanoidModel.ArmPose.SPYGLASS;
    }
}
