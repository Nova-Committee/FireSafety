package committee.nova.firesafety.common.item.impl;

import committee.nova.firesafety.FireSafety;
import committee.nova.firesafety.common.item.api.IAdvancementTriggerable;
import committee.nova.firesafety.common.item.api.IArmPoseChangeable;
import committee.nova.firesafety.common.item.api.ITagResettable;
import committee.nova.firesafety.common.item.base.FireSafetyItem;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Wearable;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import javax.annotation.ParametersAreNonnullByDefault;

import static committee.nova.firesafety.api.FireSafetyApi.*;
import static committee.nova.firesafety.common.sound.init.SoundInit.getSound;
import static committee.nova.firesafety.common.tools.PlayerHandler.notifyServerPlayer;
import static committee.nova.firesafety.common.tools.PlayerHandler.playSoundForThisPlayer;
import static committee.nova.firesafety.common.tools.format.DataFormatUtil.vec3iToLong;
import static committee.nova.firesafety.common.tools.math.RayTraceUtil.vecToIntString;
import static committee.nova.firesafety.common.tools.reference.NBTReference.FDS_CENTER;
import static committee.nova.firesafety.common.tools.reference.NBTReference.FDS_PROGRESS;
import static committee.nova.firesafety.common.tools.reference.TagKeyReference.FDS_IGNORED;
import static committee.nova.firesafety.common.tools.string.StringUtil.wrapInArrows;
import static net.minecraft.world.InteractionResultHolder.consume;
import static net.minecraft.world.InteractionResultHolder.pass;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FireDangerSnifferItem extends FireSafetyItem implements Wearable, IArmPoseChangeable, IAdvancementTriggerable, ITagResettable {
    public FireDangerSnifferItem() {
        super(new Properties().stacksTo(1).fireResistant());
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    @Override
    public EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.HEAD;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        if (world.isClientSide) return;
        if (!(entity instanceof Player player)) return;
        final var tag = stack.getOrCreateTag();
        final var p = tag.getInt(FDS_PROGRESS);
        if (p == 0) return;
        if (p == 1) {
            notifyPlayerForFireDangers(player, stack);
            tag.remove(FDS_CENTER);
        }
        tag.putInt(FDS_PROGRESS, p - 1);
    }

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        final var tag = stack.getOrCreateTag();
        if (player.isCrouching() || tag.getInt(FDS_PROGRESS) > 0 || player.getCooldowns().isOnCooldown(stack.getItem()))
            return;
        activate(player, stack);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return pass(player.getOffhandItem());
        final var stack = player.getMainHandItem();
        if (level.isClientSide) return consume(stack);
        final var tag = stack.getOrCreateTag();
        if (tag.getInt(FDS_PROGRESS) > 0) return consume(stack);
        activate(player, stack);
        return consume(stack);
    }

    private void activate(Player player, ItemStack stack) {
        final var tag = stack.getOrCreateTag();
        playSoundForThisPlayer(player, getSound(4), 1F, 1F);
        tag.putInt(FDS_PROGRESS, 80);
        tag.putLong(FDS_CENTER, vec3iToLong(player.blockPosition()));
        player.getCooldowns().addCooldown(stack.getItem(), 240);
    }

    @Override
    public HumanoidModel.ArmPose getIdlePose() {
        return HumanoidModel.ArmPose.BOW_AND_ARROW;
    }

    private void notifyPlayerForFireDangers(Player player, ItemStack stack) {
        final var tag = stack.getOrCreateTag();
        if (!tag.contains(FDS_CENTER)) {
            FireSafety.LOGGER.warn("FDS has an empty position record, this shouldn't happen!");
            return;
        }
        final var center = BlockPos.of(tag.getLong(FDS_CENTER));
        final var c1 = center.offset(-25, -25, -25);
        final var c2 = center.offset(25, 25, 25);
        final var level = player.level;
        final var blocks = BlockPos.betweenClosed(c1, c2);
        blocks.forEach(p -> {
            final var state = level.getBlockState(p);
            if (state.is(FDS_IGNORED)) return;
            final var index = getFireDangerBlockIndex(level, p);
            if (index == Short.MIN_VALUE) return;
            final var danger = getFireDangerBlock(index);
            final var dangerousness = danger.dangerousness().apply(level, p);
            final var msgO = new TranslatableComponent("msg.firesafety.fds.danger",
                    state.getBlock().getName().getString(), vecToIntString(p),
                    getDangerousness(dangerousness).getString());
            final var msgI = danger.tips().apply(level, p).withStyle(getColorFromDangerousness(dangerousness));
            notifyServerPlayer(player, getHoveredMessage(msgO, msgI));
        });
        final var entities = level.getEntitiesOfClass(Entity.class, new AABB(c1, c2),
                e -> getFireDangerEntityIndex(level, e) > Short.MIN_VALUE);
        entities.forEach(e -> {
            final var danger = getFireDangerEntity(getFireDangerEntityIndex(level, e));
            final var dangerousness = danger.dangerousness().apply(level, e);
            final var msgO = new TranslatableComponent("msg.firesafety.fds.danger",
                    e.getName().getString(), vecToIntString(e.position()),
                    getDangerousness(dangerousness).getString());
            final var msgI = danger.tips().apply(level, e).withStyle(getColorFromDangerousness(dangerousness));
            notifyServerPlayer(player, getHoveredMessage(msgO, msgI));
        });
    }

    private MutableComponent getDangerousness(int d) {
        return new TranslatableComponent("phrase.firesafety.dangerousness." + (d < 0 ? "m" + -d : String.valueOf(d)));
    }

    private ChatFormatting getColorFromDangerousness(int d) {
        return switch (d) {
            case -1 -> ChatFormatting.GOLD;
            case 0 -> ChatFormatting.DARK_GREEN;
            case 1 -> ChatFormatting.GREEN;
            case 2 -> ChatFormatting.AQUA;
            case 3 -> ChatFormatting.LIGHT_PURPLE;
            case 4 -> ChatFormatting.RED;
            default -> ChatFormatting.WHITE;
        };
    }

    private MutableComponent getHoveredMessage(Component outer, Component inner) {
        return wrapInArrows(outer.copy()).withStyle(e -> e.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, inner)));
    }

    @Override
    public ResourceLocation getAdvancement() {
        return new ResourceLocation(FireSafety.MODID, "prevention_is_better_than_cure");
    }

    @Override
    public void resetTagOnDimensionChange(ItemStack stack) {
        final var tag = stack.getOrCreateTag();
        tag.putInt(FDS_PROGRESS, 0);
        tag.remove(FDS_CENTER);
    }
}
