package committee.nova.firesafety.api.event;

import committee.nova.firesafety.common.block.blockEntity.impl.ExtinguisherBlockEntity;
import committee.nova.firesafety.common.entity.impl.projectile.WaterSprayProjectile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@ParametersAreNonnullByDefault
public class FireExtinguishedEvent extends Event {
    public enum ExtinguisherType {
        DEVICE,
        HANDHELD
    }

    private final ExtinguisherType extinguisherType;
    private final Level level;
    private final BlockPos extinguisherPos;
    private final Entity extinguisherEntity;
    private final BlockPos extinguishedPos;
    private final Entity extinguishedEntity;
    private final short priority;
    private final boolean isEntityExtinguished;

    public FireExtinguishedEvent(ExtinguisherType extinguisherType, Level level, @Nullable BlockPos extinguisherPos, @Nullable Entity extinguisherEntity, boolean isEntityExtinguished, @Nullable BlockPos extinguishedPos, @Nullable Entity extinguishedEntity, short priority) {
        this.extinguisherType = extinguisherType;
        Objects.requireNonNull(extinguisherType == ExtinguisherType.DEVICE ? extinguisherPos : extinguisherEntity);
        this.extinguisherPos = extinguisherPos;
        this.extinguisherEntity = extinguisherEntity;
        this.level = level;
        this.isEntityExtinguished = isEntityExtinguished;
        Objects.requireNonNull(isEntityExtinguished ? extinguishedEntity : extinguishedPos);
        this.extinguishedPos = extinguishedPos;
        this.extinguishedEntity = extinguishedEntity;
        this.priority = priority;
    }

    public FireExtinguishedEvent(ExtinguisherType extinguisherType, Level level, BlockPos extinguisherPos, Entity extinguishedEntity, short priority) {
        this(extinguisherType, level, extinguisherPos, null, true, null, extinguishedEntity, priority);
    }

    public FireExtinguishedEvent(ExtinguisherType extinguisherType, Level level, Entity extinguisherEntity, Entity extinguishedEntity, short priority) {
        this(extinguisherType, level, null, extinguisherEntity, true, null, extinguishedEntity, priority);
    }

    public FireExtinguishedEvent(ExtinguisherType extinguisherType, Level level, BlockPos extinguisherPos, BlockPos pos, short priority) {
        this(extinguisherType, level, extinguisherPos, null, false, pos, null, priority);
    }

    public FireExtinguishedEvent(ExtinguisherType extinguisherType, Level level, Entity extinguisherEntity, BlockPos pos, short priority) {
        this(extinguisherType, level, null, extinguisherEntity, false, pos, null, priority);
    }

    public Entity getEntityExtinguished() {
        return extinguishedEntity;
    }

    public ExtinguisherType getExtinguisherType() {
        return extinguisherType;
    }

    public short getPriority() {
        return priority;
    }

    public BlockPos getExtinguishedPos() {
        return extinguishedPos;
    }

    public boolean getExtinguishedEntity() {
        return isEntityExtinguished;
    }

    public BlockPos getExtinguisherPos() {
        return extinguisherPos;
    }

    public Entity getExtinguisherEntity() {
        return extinguisherEntity;
    }

    public boolean isBlockExtinguished() {
        return !isEntityExtinguished;
    }

    @Nullable
    public Entity getExtinguisher() {
        if (extinguisherEntity instanceof final WaterSprayProjectile w) return w.getOwner();
        final var server = level.getServer();
        if (server == null) return null;
        final var blockEntity = level.getBlockEntity(extinguisherPos);
        return (blockEntity instanceof final ExtinguisherBlockEntity e) ? e.getOwner() : null;
    }
}
