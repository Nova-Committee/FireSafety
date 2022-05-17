package committee.nova.firesafety.common.entity.impl.projectile;

import committee.nova.firesafety.api.FireSafetyApi;
import committee.nova.firesafety.common.entity.init.EntityInit;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.ParametersAreNonnullByDefault;

import static committee.nova.firesafety.common.tools.reference.ItemReference.WATER_BOMB;
import static committee.nova.firesafety.common.tools.reference.ItemReference.getRegisteredItem;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WaterBombProjectile extends AbstractArrow {
    public WaterBombProjectile(EntityType<? extends AbstractArrow> e, Level l) {
        super(e, l);
    }

    public WaterBombProjectile(double x, double y, double z, Level l) {
        super(EntityInit.waterBomb.get(), x, y, z, l);
    }

    public WaterBombProjectile(LivingEntity f, Level l) {
        super(EntityInit.waterBomb.get(), f, l);
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    public ItemStack getItem() {
        return getRegisteredItem(WATER_BOMB).getDefaultInstance();
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.CANDLE_EXTINGUISH;
    }

    @Override
    protected void onHitEntity(EntityHitResult r) {
        var entity = r.getEntity();
        final var i = FireSafetyApi.getTargetEntityIndex(level, entity);
        if (i != Short.MIN_VALUE) {
            final var t = FireSafetyApi.getTargetEntity(i);
            t.entityAction().accept(level, entity);
        }
        discard();
    }

    @Override
    public void tick() {
        if (isRemoved()) return;
        super.tick();
        final var timeOut = tickCount > 1000;
        if (timeOut || isInWaterOrBubble()) {
            //todo: own item
            drop();
            discard();
            return;
        }
        final var p = blockPosition();
        if (level.getBlockState(p).is(Blocks.FIRE)) level.setBlockAndUpdate(p, Blocks.AIR.defaultBlockState());
        if (inGround || onGround) extinguish();
    }

    private void extinguish() {
        final var pos = getOnPos();
        final var posList = BlockPos.betweenClosed(pos.offset(0, -5, 0), pos.offset(0, 3, 0));
        posList.forEach(b -> {
            final var i = FireSafetyApi.getTargetBlockIndex(level, b);
            if (i > Short.MIN_VALUE) {
                final var t = FireSafetyApi.getTargetBlock(i);
                level.setBlockAndUpdate(b, t.targetBlock().apply(level, b));
                t.extinguishedInfluence().accept(level, b);
            }
        });
        discard();
    }

    private void drop() {
        level.addFreshEntity(new ItemEntity(level, getX(), getY(), getZ(), getItem()));
    }


    public static void bombard(Level world, BlockPos pos) {
        final var random = world.random;
        final var bomb = new WaterBombProjectile(pos.getX() + .5 + random.nextDouble(-.2, .4), pos.getY() + random.nextDouble(-.2, .4), pos.getZ() + .5 + random.nextDouble(-.2, .4), world);
        bomb.shoot(0, -1, 0, .2F, 0);
        bomb.setNoGravity(true);
        bomb.setDeltaMovement(new Vec3(0, -.5, 0));
        bomb.setSilent(true);
        bomb.setCritArrow(false);
        world.addFreshEntity(bomb);
    }

    @Override
    public void onRemovedFromWorld() {
        for (int i = 0; i < 10; i++) {
            level.addAlwaysVisibleParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, getX() + random.nextDouble(-.3, .6), getY(), getZ() + random.nextDouble(-.3, .6), 0, 0, 0);
        }
    }
}
