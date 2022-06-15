package committee.nova.firesafety.common.item.impl;

import committee.nova.firesafety.common.item.api.IArmPoseChangeable;
import committee.nova.firesafety.common.item.base.FireSafetyItem;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;

import static committee.nova.firesafety.common.entity.projectile.impl.WaterBombProjectile.bombard;
import static committee.nova.firesafety.common.tools.format.DataFormatUtil.vec3ToLong;
import static committee.nova.firesafety.common.tools.math.RayTraceUtil.*;
import static committee.nova.firesafety.common.tools.misc.PlayerHandler.notifyServerPlayer;
import static committee.nova.firesafety.common.tools.misc.PlayerHandler.playSoundForThisPlayer;
import static committee.nova.firesafety.common.tools.reference.ItemReference.FIREFIGHTING_AIRSTRIKE_CONTROLLER;
import static committee.nova.firesafety.common.tools.reference.ItemReference.getRegisteredItem;
import static committee.nova.firesafety.common.tools.reference.NBTReference.*;
import static committee.nova.firesafety.common.tools.sound.init.SoundInit.getSound;
import static committee.nova.firesafety.common.tools.string.StringUtil.formattedNumber;
import static java.lang.Math.*;
import static net.minecraft.core.BlockPos.betweenClosed;
import static net.minecraft.util.Mth.hsvToRgb;
import static net.minecraft.world.InteractionResultHolder.consume;
import static net.minecraft.world.InteractionResultHolder.pass;
import static net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FireFightingAirStrikeControllerItem extends FireSafetyItem implements IArmPoseChangeable {

    public FireFightingAirStrikeControllerItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return pass(player.getOffhandItem());
        final var stack = player.getMainHandItem();
        if (level.isClientSide) return consume(stack);
        final var tag = stack.getOrCreateTag();
        final var confirm = tag.getBoolean(FFASC_CONFIRM);
        if (confirm) {
            if (player.isCrouching()) {
                playSoundForThisPlayer(player, getSound(1), 1F, 1F);
                notifyServerPlayer(player, Component.translatable("msg.firesafety.ffasc.cancelled"));
                tag.putBoolean(FFASC_CONFIRM, false);
                return consume(stack);
            }
            if (!level.dimension().location().toString().equals(tag.getString(FFASC_DIM))) {
                notifyServerPlayer(player, Component.translatable("msg.firesafety.ffasc.different_dim"));
                return consume(stack);
            }
            notifyServerPlayer(player, Component.translatable("msg.firesafety.ffasc.confirmed",
                    vecToIntString(getVecByPos(BlockPos.of(tag.getLong(FFASC_CENTER))))));
            playSoundForThisPlayer(player, getSound(3), 1F, 1F);
            launch(stack, player);
            tag.putInt(FFASC_COMMON_PREPARATION, tag.getInt(FFASC_COMMON_PREPARATION) + 1200);
            player.getCooldowns().addCooldown(getRegisteredItem(FIREFIGHTING_AIRSTRIKE_CONTROLLER), 60);
            tag.putBoolean(FFASC_CONFIRM, false);
            return consume(stack);
        }
        if (player.isCrouching()) {
            //todo(mode)
            return consume(stack);
        }
        final var trace = getRaytracingBlock(player);
        if (tag.getInt(FFASC_COMMON_PREPARATION) > 4800) {
            notifyServerPlayer(player, Component.translatable("msg.firesafety.ffasc.not_prepared"));
            return consume(stack);
        }
        if (trace == null) {
            notifyServerPlayer(player, Component.translatable("msg.firesafety.ffasc.too_far"));
            return consume(stack);
        }
        if (level.dimensionTypeRegistration().value().hasCeiling()) {
            notifyServerPlayer(player, Component.translatable("msg.firesafety.ffasc.unreachable_dim"));
            return consume(stack);
        }
        //todo: water bomb item check && consumption
        tag.putString(FFASC_DIM, level.dimension().location().toString());
        tag.putLong(FFASC_CENTER, vec3ToLong(trace));
        playSoundForThisPlayer(player, getSound(1), 1F, 1F);
        notifyServerPlayer(player, Component.translatable("msg.firesafety.ffasc.confirm_query", vecToIntString(trace)));
        tag.putBoolean(FFASC_CONFIRM, true);
        return consume(stack);
    }


    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        if (level.isClientSide) return;
        if (!(entity instanceof final Player player)) return;
        if (isSelected) displayInformation(stack, player);
        final var tag = stack.getOrCreateTag();
        tag.putBoolean(USING, isSelected);
        final var common = tag.getInt(FFASC_COMMON_PREPARATION);
        if (common % 1200 == 1) playSoundForThisPlayer(player, getSound(2), 1F, 1F);
        if (common > 0) tag.putInt(FFASC_COMMON_PREPARATION, common - 1);
        //todo: final var mode = tag.getInt(FFASC_MODE);
    }

    private void launch(ItemStack stack, Player player) {
        final var tag = stack.getOrCreateTag();
        final var center = BlockPos.of(tag.getLong(FFASC_CENTER));
        final var list = betweenClosed(center.offset(5, 0, 5), center.offset(-5, 0, -5));
        int hRaw = 0;
        final var level = player.level;
        for (final var pos : list) {
            final var h1 = player.level.getHeightmapPos(MOTION_BLOCKING, pos).getY();
            if (h1 > hRaw) hRaw = h1;
        }
        final var h = min(hRaw + 35, level.getMaxBuildHeight());
        list.forEach(p -> bombard(level, new BlockPos(p.getX(), h, p.getZ())));
    }

    private void displayInformation(ItemStack stack, Player player) {
        final var trace = getRaytracingBlock(player);
        final var location = trace != null ?
                Component.translatable("info.firesafety.ffasc.location", vecToIntString(trace)).getString() :
                "[" + Component.translatable("info.firesafety.ffasc.too_far").getString() + "]";
        final var tag = stack.getOrCreateTag();
        final var progress = tag.getInt(FFASC_COMMON_PREPARATION);
        final var load = Component.translatable("info.firesafety.ffasc.reload", (progress != 0) ?
                formattedNumber((1200F - progress % 1200F) / 12F, 0) + "%/" +
                        formattedNumber((6000F - progress) / 60F, 0) + "%" :
                Component.translatable("phrase.firesafety.finished").getString()).getString();
        final var c = 100F - player.getCooldowns().getCooldownPercent(stack.getItem(), 0.0F) * 100F;
        final var cd = Component.translatable("info.firesafety.ffasc.cd", c != 100F ?
                formattedNumber(c, 0) + "%" :
                Component.translatable("phrase.firesafety.finished").getString()).getString();
        player.displayClientMessage(Component.literal(location + " " + load + " " + cd), true);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        final var f = max(0.0F, (6000 - (float) stack.getOrCreateTag().getInt(FFASC_COMMON_PREPARATION)) / (float) 6000);
        return hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return round(13.0F - (float) stack.getOrCreateTag().getInt(FFASC_COMMON_PREPARATION) * 13.0F / (float) 6000);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }
}
