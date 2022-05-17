package committee.nova.firesafety.common.item.impl;

import committee.nova.firesafety.common.entity.impl.projectile.WaterBombProjectile;
import committee.nova.firesafety.common.item.base.FireSafetyItem;
import committee.nova.firesafety.common.tools.PlayerHandler;
import committee.nova.firesafety.common.tools.math.RayTraceUtil;
import committee.nova.firesafety.common.tools.string.StringUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

import javax.annotation.ParametersAreNonnullByDefault;

import static committee.nova.firesafety.common.tools.reference.ItemReference.FIREFIGHTING_AIRSTRIKE_CONTROLLER;
import static committee.nova.firesafety.common.tools.reference.ItemReference.getRegisteredItem;
import static committee.nova.firesafety.common.tools.reference.NBTReference.*;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FireFightingAirStrikeControllerItem extends FireSafetyItem {

    public FireFightingAirStrikeControllerItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(player.getOffhandItem());
        final var stack = player.getMainHandItem();
        if (level.isClientSide) return InteractionResultHolder.success(stack);
        final var tag = stack.getOrCreateTag();
        final var confirm = tag.getBoolean(FFASC_CONFIRM);
        if (confirm) {
            if (player.isCrouching()) {
                PlayerHandler.notifyServerPlayer(player, new TranslatableComponent("msg.firesafety.ffasc.cancelled"));
                tag.putBoolean(FFASC_CONFIRM, false);
                return InteractionResultHolder.success(stack);
            }
            PlayerHandler.notifyServerPlayer(player, new TranslatableComponent("msg.firesafety.ffasc.confirmed",
                    RayTraceUtil.vecToIntString(RayTraceUtil.getVecByPos(BlockPos.of(tag.getLong(FFASC_CENTER))))));
            tag.putInt(FFASC_PROGRESS, 200);
            tag.putInt(FFASC_COMMON_PREPARATION, tag.getInt(FFASC_COMMON_PREPARATION) + 1200);
            player.getCooldowns().addCooldown(getRegisteredItem(FIREFIGHTING_AIRSTRIKE_CONTROLLER), 400);
            tag.putBoolean(FFASC_CONFIRM, false);
            return InteractionResultHolder.success(stack);
        }
        if (player.isCrouching()) {
            //todo(mode)
            return InteractionResultHolder.success(stack);
        }
        final var trace = RayTraceUtil.getRaytracingBlock(player);
        if (tag.getInt(FFASC_COMMON_PREPARATION) > 4800) {
            PlayerHandler.notifyServerPlayer(player, new TranslatableComponent("msg.firesafety.ffasc.not_prepared"));
            return InteractionResultHolder.success(stack);
        }
        if (trace == null) {
            PlayerHandler.notifyServerPlayer(player, new TranslatableComponent("msg.firesafety.ffasc.too_far"));
            return InteractionResultHolder.success(stack);
        }
        //todo: water bomb item check && consumption
        tag.putLong(FFASC_CENTER, BlockPos.asLong((int) trace.x, (int) trace.y, (int) trace.z));
        PlayerHandler.notifyServerPlayer(player, new TranslatableComponent("msg.firesafety.ffasc.confirm_query", RayTraceUtil.vecToIntString(trace)));
        tag.putBoolean(FFASC_CONFIRM, true);
        return InteractionResultHolder.success(stack);
    }


    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        if (level.isClientSide) return;
        if (!(entity instanceof Player player)) return;
        if (isSelected) displayInformation(stack, player);
        final var tag = stack.getOrCreateTag();
        final var common = tag.getInt(FFASC_COMMON_PREPARATION);
        if (common > 0) tag.putInt(FFASC_COMMON_PREPARATION, common - 1);
        //final var mode = tag.getInt(FFASC_MODE);
        final var progress = tag.getInt(FFASC_PROGRESS);
        if (progress == 0) return;
        if (progress == 1) launch(stack, player);
        tag.putInt(FFASC_PROGRESS, progress - 1);
    }

    private void launch(ItemStack stack, Player player) {
        PlayerHandler.playSoundForThisPlayer(player, SoundEvents.FIREWORK_ROCKET_SHOOT, 1F, 1F);
        final var tag = stack.getOrCreateTag();
        final var center = BlockPos.of(tag.getLong(FFASC_CENTER));
        final var list = BlockPos.betweenClosed(center.offset(5, 0, 5), center.offset(-5, 0, -5));
        int h = 0;
        final var level = player.level;
        for (final var pos : list) {
            final var h1 = player.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos).getY();
            if (h1 > h) h = h1;
        }
        for (final var pos : list)
            WaterBombProjectile.bombard(level, new BlockPos(pos.getX(), Math.min(h + 20, level.getMaxBuildHeight()), pos.getZ()));
    }

    private void displayInformation(ItemStack stack, Player player) {
        final var trace = RayTraceUtil.getRaytracingBlock(player);
        final var location = trace != null ?
                new TranslatableComponent("info.firesafety.ffasc.location", RayTraceUtil.vecToIntString(trace)).getString() :
                "[" + new TranslatableComponent("info.firesafety.ffasc.too_far").getString() + "]";
        final var tag = stack.getOrCreateTag();
        final var progress = tag.getInt(FFASC_COMMON_PREPARATION);
        final var load = new TranslatableComponent("info.firesafety.ffasc.reload", (progress != 0) ?
                StringUtil.formattedNumber((1200F - progress % 1200F) / 12F, 0) + "%/" +
                        StringUtil.formattedNumber((6000F - progress) / 60F, 0) + "%" :
                new TranslatableComponent("phrase.firesafety.finished").getString()).getString();
        final var c = 100F - player.getCooldowns().getCooldownPercent(stack.getItem(), 0.0F) * 100F;
        final var cd = new TranslatableComponent("info.firesafety.ffasc.cd", c != 100F ?
                StringUtil.formattedNumber(c, 0) + "%" :
                new TranslatableComponent("phrase.firesafety.finished").getString()).getString();
        player.displayClientMessage(new TextComponent(location + " " + load + " " + cd), true);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    @Override
    public int getDamage(ItemStack stack) {
        return stack.getOrCreateTag().getInt(FFASC_COMMON_PREPARATION);
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return 6000;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        final var f = Math.max(0.0F, (6000 - (float) stack.getOrCreateTag().getInt(FFASC_COMMON_PREPARATION)) / (float) 6000);
        return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F - (float) stack.getOrCreateTag().getInt(FFASC_COMMON_PREPARATION) * 13.0F / (float) 6000);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }
}