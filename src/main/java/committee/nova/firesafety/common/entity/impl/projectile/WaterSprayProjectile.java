package committee.nova.firesafety.common.entity.impl.projectile;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

import javax.annotation.ParametersAreNonnullByDefault;

import static committee.nova.firesafety.api.FireSafetyApi.*;
import static committee.nova.firesafety.common.entity.init.EntityInit.waterSpray;
import static net.minecraft.core.BlockPos.betweenClosed;
import static net.minecraft.core.particles.ParticleTypes.CAMPFIRE_COSY_SMOKE;
import static net.minecraft.sounds.SoundEvents.CANDLE_EXTINGUISH;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WaterSprayProjectile extends AbstractArrow implements ItemSupplier {
    public WaterSprayProjectile(EntityType<? extends AbstractArrow> e, Level l) {
        super(e, l);
    }

    public WaterSprayProjectile(double x, double y, double z, Level l) {
        super(waterSpray.get(), x, y, z, l);
    }

    public WaterSprayProjectile(LivingEntity e, Level l) {
        this(e.getX() + e.getLookAngle().x, e.getEyeY() + e.getLookAngle().y - 0.1, e.getZ() + e.getLookAngle().z, l);
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getItem() {
        return getPickupItem();
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return CANDLE_EXTINGUISH;
    }

    @Override
    public void tick() {
        super.tick();
        if (tickCount > 200) {
            discard();
            return;
        }
        setDeltaMovement(getDeltaMovement().add(0, -.001, 0));
        if (tickCount % 2 == 0)
            level.addParticle(CAMPFIRE_COSY_SMOKE, getX(), getY(), getZ(), getDeltaMovement().x * .01, -.01, getDeltaMovement().z * .01);
        final var blocks = betweenClosed(blockPosition(), blockPosition().offset(1, 1, 1));
        extinguishBlocks(blocks);
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        extinguishNearby();
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        extinguishNearby();
        final var pos = hit.getBlockPos();
        if (level.getBlockState(pos).isCollisionShapeFullBlock(level, pos)) discard();
    }

    public static void spray(LivingEntity entity) {
        final var world = entity.level;
        final var water = new WaterSprayProjectile(entity, world);
        final var r = world.random;
        final var look = entity.getLookAngle();
        water.shoot(look.x + r.nextFloat(.1F), look.y + r.nextFloat(.1F), look.z + r.nextFloat(.01F), .3F, 0);
        water.setNoGravity(true);
        water.setSilent(true);
        water.setCritArrow(false);
        world.addFreshEntity(water);
    }

    private void extinguishNearby() {
        final var lC = blockPosition().offset(-1, -1, -1);
        final var rC = blockPosition().offset(1, 1, 1);
        extinguishBlocks(betweenClosed(lC, rC));
        extinguishEntities(level.getEntitiesOfClass(Entity.class, new AABB(lC, rC), e -> getTargetEntityIndex(level, e) > Short.MIN_VALUE));
        discard();
    }

    private void extinguishBlocks(final Iterable<BlockPos> blocks) {
        blocks.forEach(p -> {
            final short i = getTargetBlockIndex(level, p);
            if (i == Short.MIN_VALUE) return;
            final var t = getTargetBlock(i);
            level.setBlockAndUpdate(p, t.targetBlock().apply(level, p));
            t.extinguishedInfluence().accept(level, p);
        });
    }

    private void extinguishEntities(Iterable<Entity> entities) {
        entities.forEach(e -> {
            final var t = getTargetEntity(getTargetEntityIndex(level, e));
            t.entityAction().accept(level, e);
        });
    }
}
