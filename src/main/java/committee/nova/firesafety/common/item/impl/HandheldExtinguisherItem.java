package committee.nova.firesafety.common.item.impl;

import committee.nova.firesafety.FireSafety;
import committee.nova.firesafety.common.capabilities.HandheldExtinguisherFluidHandler;
import committee.nova.firesafety.common.item.api.IAdvancementTriggerable;
import committee.nova.firesafety.common.item.api.IArmPoseChangeable;
import committee.nova.firesafety.common.item.base.FireSafetyItem;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

import static committee.nova.firesafety.common.entity.projectile.impl.WaterSprayProjectile.spray;
import static committee.nova.firesafety.common.tools.misc.FluidUtil.tryFill;
import static committee.nova.firesafety.common.tools.misc.PlayerHandler.displayClientMessage;
import static committee.nova.firesafety.common.tools.misc.PlayerHandler.getActionByMode;
import static committee.nova.firesafety.common.tools.reference.NBTReference.USING;
import static java.lang.Math.max;
import static java.lang.Math.round;
import static net.minecraft.sounds.SoundEvents.BUCKET_EMPTY;
import static net.minecraft.sounds.SoundEvents.CANDLE_EXTINGUISH;
import static net.minecraft.sounds.SoundSource.BLOCKS;
import static net.minecraft.util.Mth.hsvToRgb;
import static net.minecraft.world.InteractionResultHolder.consume;
import static net.minecraft.world.InteractionResultHolder.pass;
import static net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class HandheldExtinguisherItem extends FireSafetyItem implements IArmPoseChangeable, IAdvancementTriggerable {
    public HandheldExtinguisherItem() {
        super(new Properties().stacksTo(1).fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        final var stack = player.getItemInHand(hand);
        if (hand == InteractionHand.OFF_HAND) return pass(stack);
        if (!player.isCrouching()) {
            player.startUsingItem(hand);
            return consume(stack);
        }
        final var op = getFluid(stack);
        if (op.isEmpty()) return consume(stack);
        final var fluid = op.get();
        final int needed = fluid.getTankCapacity(0) - fluid.getFluidInTank(0).getAmount();
        if (needed == 0) return consume(stack);
        final var off = player.getOffhandItem();
        final var toFill = tryFill(player, InteractionHand.OFF_HAND, needed, off);
        if (toFill.isEmpty()) return consume(stack);
        fluid.fill(toFill, IFluidHandler.FluidAction.EXECUTE);
        level.playSound(null, player, BUCKET_EMPTY, BLOCKS, 1F, 1F);
        player.getCooldowns().addCooldown(stack.getItem(), 100);
        return consume(stack);
    }

    @Override
    public void onUsingTick(ItemStack stack, LivingEntity entity, int count) {
        final var level = entity.level;
        if (level.isClientSide || !(entity instanceof final Player player)) return;
        final var cap = getFluid(stack);
        if (cap.isEmpty()) return;
        final var fluid = cap.get();
        final var amount = fluid.getFluidInTank(0).getAmount();
        if (amount < 1) return;
        if ((level.getDayTime() & 1) == 1)
            level.playSound(null, entity, CANDLE_EXTINGUISH, SoundSource.PLAYERS, .7F, 1.6F);
        final var consumed = fluid.drain(20, getActionByMode(player));
        extinguish(player, consumed.getAmount());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        if (level.isClientSide || !(entity instanceof final Player player)) return;
        if (player.getOffhandItem() == stack)
            displayClientMessage(player, new TranslatableComponent("msg.firesafety.main_hand", stack.getItem().getName(stack).getString()));
        final var tag = stack.getOrCreateTag();
        tag.putBoolean(USING, isSelected && player.isUsingItem());
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tips, TooltipFlag flag) {
        final var op = getFluid(stack);
        if (op.isEmpty()) return;
        final var fluid = op.get();
        tips.add(new TranslatableComponent("tooltips.firesafety.water_amount", fluid.getFluidInTank(0).getAmount(), fluid.getTankCapacity(0)));
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public ResourceLocation getAdvancement() {
        return new ResourceLocation(FireSafety.MODID, "pass");
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        //todo: configurable
        return new HandheldExtinguisherFluidHandler(stack, 10000);
    }

    public Optional<IFluidHandlerItem> getFluid(ItemStack stack) {
        return stack.getCapability(FLUID_HANDLER_ITEM_CAPABILITY).resolve();
    }

    @Override
    public int getBarColor(ItemStack stack) {
        final var f = max(0.0F, getWaterSufficiency(stack));
        return hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return round(getWaterSufficiency(stack) * 13.0F);
    }

    private float getWaterSufficiency(ItemStack stack) {
        final var op = getFluid(stack);
        if (op.isEmpty()) return 0F;
        final var fluid = op.get();
        return (float) fluid.getFluidInTank(0).getAmount() / (float) fluid.getTankCapacity(0);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    private void extinguish(Player player, int consumed) {
        final var amount = consumed / 8;
        for (var i = 0; i < amount; i++) {
            spray(player);
        }
    }
}
