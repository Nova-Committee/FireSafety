package committee.nova.firesafety.common.entity.projectile.base;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FunctionalProjectile extends Projectile {
    private static final EntityDataAccessor<Byte> ID_FLAGS = SynchedEntityData.defineId(FunctionalProjectile.class, EntityDataSerializers.BYTE);
    protected boolean inGround;
    @Nullable
    private BlockState lastState;

    protected FunctionalProjectile(EntityType<? extends Projectile> e, Level l) {
        super(e, l);
    }

    public FunctionalProjectile(EntityType<? extends FunctionalProjectile> e, double x, double y, double z, Level l) {
        this(e, l);
        this.setPos(x, y, z);
    }

    public FunctionalProjectile(EntityType<? extends FunctionalProjectile> e, LivingEntity f, Level l) {
        this(e, f.getX(), f.getEyeY() - (double) 0.1F, f.getZ(), l);
        this.setOwner(f);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(ID_FLAGS, (byte) 0);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        final double dRaw = this.getBoundingBox().getSize() * 10.0D;
        final double v = 128.0D * getViewScale();
        final double d0 = (Double.isNaN(dRaw) ? 1.0D : dRaw) * v;
        return d < d0 * d0;
    }

    @Override
    public void lerpTo(double x, double y, double z, float xRot, float yRot, int i, boolean b) {
        this.setPos(x, y, z);
        this.setRot(xRot, yRot);
    }

    public void tick() {
        if (isRemoved()) return;
        super.tick();
        final var flag = this.isNoPhysics();
        var vec3A = this.getDeltaMovement();
        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            double d0 = vec3A.horizontalDistance();
            this.setYRot((float) (Mth.atan2(vec3A.x, vec3A.z) * (180F / Math.PI)));
            this.setXRot((float) (Mth.atan2(vec3A.y, d0) * (180F / Math.PI)));
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        }

        final var pos = this.blockPosition();
        final var state = this.level.getBlockState(pos);
        if (!state.isAir() && !flag) {
            final var shape = state.getCollisionShape(this.level, pos);
            if (!shape.isEmpty()) {
                final var vec3B = this.position();
                final var aabbs = shape.toAabbs();
                for (final var aabb : aabbs) {
                    if (aabb.move(pos).contains(vec3B)) {
                        this.inGround = true;
                        break;
                    }
                }
            }
        }

        if (this.inGround && !flag) {
            if (this.lastState != state && this.shouldFall()) this.startFalling();
            return;
        }
        final var vec3C = this.position();
        var vec3D = vec3C.add(vec3A);
        HitResult hitresult = this.level.clip(new ClipContext(vec3C, vec3D, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (hitresult.getType() != HitResult.Type.MISS) {
            vec3D = hitresult.getLocation();
        }
        while (!this.isRemoved()) {
            var entityhitresult = this.findHitEntity(vec3C, vec3D);
            if (entityhitresult != null) {
                hitresult = entityhitresult;
            }
            if (hitresult != null && hitresult.getType() == HitResult.Type.ENTITY && hitresult instanceof final EntityHitResult e) {
                final var entity = e.getEntity();
                final var entity1 = this.getOwner();
                if (entity instanceof final Player p1 && entity1 instanceof final Player p2 && !(p2).canHarmPlayer(p1)) {
                    hitresult = null;
                    entityhitresult = null;
                }
            }
            if (hitresult != null && hitresult.getType() != HitResult.Type.MISS && !flag && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, hitresult)) {
                this.onHit(hitresult);
                this.hasImpulse = true;
            }
            if (entityhitresult == null) break;
            hitresult = null;
        }
        vec3A = this.getDeltaMovement();
        final var d5 = vec3A.x;
        final var d6 = vec3A.y;
        final var d1 = vec3A.z;
        final var d7 = this.getX() + d5;
        final var d2 = this.getY() + d6;
        final var d3 = this.getZ() + d1;
        final var d4 = vec3A.horizontalDistance();
        this.setYRot(flag ? (float) (Mth.atan2(-d5, -d1) * (180F / Math.PI)) : (float) (Mth.atan2(d5, d1) * (180F / Math.PI)));
        this.setXRot((float) (Mth.atan2(d6, d4) * (180F / Math.PI)));
        this.setXRot(lerpRotation(this.xRotO, this.getXRot()));
        this.setYRot(lerpRotation(this.yRotO, this.getYRot()));
        this.setDeltaMovement(vec3A.scale(0.99F));
        if (!this.isNoGravity() && !flag) {
            final var vec3E = this.getDeltaMovement();
            this.setDeltaMovement(vec3E.x, vec3E.y - (double) 0.05F, vec3E.z);
        }
        this.setPos(d7, d2, d3);
        this.checkInsideBlocks();
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        this.lastState = this.level.getBlockState(hit.getBlockPos());
        super.onHitBlock(hit);
        final var vec3 = hit.getLocation().subtract(this.getX(), this.getY(), this.getZ());
        this.setDeltaMovement(vec3);
        var vec31 = vec3.normalize().scale(0.05F);
        this.setPosRaw(this.getX() - vec31.x, this.getY() - vec31.y, this.getZ() - vec31.z);
        this.inGround = true;
    }

    public boolean isNoPhysics() {
        return this.level.isClientSide ? (this.entityData.get(ID_FLAGS) & 2) != 0 : this.noPhysics;
    }

    public void setNoPhysics(boolean b) {
        this.noPhysics = b;
        this.setFlag(2, b);
    }

    private boolean shouldFall() {
        return this.inGround && this.level.noCollision((new AABB(this.position(), this.position())).inflate(0.06D));
    }

    private void startFalling() {
        this.inGround = false;
        this.setDeltaMovement(this.getDeltaMovement().multiply(this.random.nextFloat() * 0.2F, this.random.nextFloat() * 0.2F, this.random.nextFloat() * 0.2F));
    }

    @Nullable
    protected EntityHitResult findHitEntity(Vec3 v1, Vec3 v2) {
        return ProjectileUtil.getEntityHitResult(this.level, this, v1, v2, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0D), this::canHitEntity);
    }

    private void setFlag(int i, boolean b) {
        final byte b0 = this.entityData.get(ID_FLAGS);
        if (b) {
            this.entityData.set(ID_FLAGS, (byte) (b0 | i));
            return;
        }
        this.entityData.set(ID_FLAGS, (byte) (b0 & ~i));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.lastState != null) tag.put("inBlockState", NbtUtils.writeBlockState(this.lastState));
        tag.putBoolean("inGround", this.inGround);
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("inBlockState", 10)) {
            this.lastState = NbtUtils.readBlockState(tag.getCompound("inBlockState"));
        }
        this.inGround = tag.getBoolean("inGround");
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    protected MovementEmission getMovementEmission() {
        return MovementEmission.NONE;
    }

    @Override
    protected float getEyeHeight(Pose pose, EntityDimensions dims) {
        return 0.13F;
    }
}