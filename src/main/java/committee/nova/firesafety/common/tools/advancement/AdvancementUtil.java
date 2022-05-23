package committee.nova.firesafety.common.tools.advancement;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class AdvancementUtil {
    public static void tryAwardAdvancement(ServerPlayer player, ResourceLocation advancementId) {
        if (player == null) {
            return;
        }
        final var advancement = player.server.getAdvancements().getAdvancement(advancementId);
        if (advancement == null) {
            return;
        }
        final var progress = player.getAdvancements().getOrStartProgress(advancement);
        if (!progress.isDone()) completeTheAdvancement(player, advancement, progress);
    }

    private static void completeTheAdvancement(ServerPlayer player, Advancement advancement, AdvancementProgress progress) {
        final var iterator = progress.getRemainingCriteria().iterator();
        if (iterator.hasNext()) {
            player.getAdvancements().award(advancement, iterator.next());
            completeTheAdvancement(player, advancement, progress);
        }
    }

}