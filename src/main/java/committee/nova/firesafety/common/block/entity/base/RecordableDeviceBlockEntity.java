package committee.nova.firesafety.common.block.entity.base;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

import static net.minecraft.nbt.Tag.TAG_COMPOUND;

@ParametersAreNonnullByDefault
public abstract class RecordableDeviceBlockEntity extends BlockEntity {
    private Entity cachedOwner;
    private UUID ownerUUID;
    private final HashMap<UUID, Boolean> notifies = new HashMap<>();

    public RecordableDeviceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void load(CompoundTag tag) {
        final var listeners = tag.getList("listeners", TAG_COMPOUND);
        synchronized (notifies) {
            notifies.clear();
            if (listeners.isEmpty()) return;
            final int lSize = listeners.size();
            for (int i = 0; i < lSize; i++) {
                final var s = listeners.getCompound(i);
                notifies.put(s.getUUID("uuid"), s.getBoolean("on"));
            }
        }
        if (tag.hasUUID("owner")) ownerUUID = tag.getUUID("owner");
        super.load(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (notifies.isEmpty()) return;
        final var listeners = new ListTag();
        for (final var uuid : notifies.keySet()) {
            final var u = new CompoundTag();
            u.putUUID("uuid", uuid);
            u.putBoolean("on", notifies.get(uuid));
            listeners.add(u);
        }
        tag.put("listeners", listeners);
        if (ownerUUID != null) tag.putUUID("owner", ownerUUID);
    }

    public boolean handleListener(Player player) {
        final var u = player.getUUID();
        if (!notifies.containsKey(u) || !notifies.get(u)) {
            notifies.put(u, true);
            return true;
        }
        notifies.remove(u);
        return false;
    }

    public void toListeningPlayers(Level level, Consumer<Player> action) {
        final var server = level.getServer();
        if (server == null) return;
        final var list = server.getPlayerList();
        for (final var u : notifies.keySet()) {
            if (!notifies.containsKey(u) || !notifies.get(u)) continue;
            final var p = list.getPlayer(u);
            if (p == null) continue;
            action.accept(p);
        }
    }

    @Override
    public String toString() {
        final var b = new StringBuilder();
        for (final UUID u : notifies.keySet())
            if (notifies.get(u)) {
                b.append(" ");
                b.append(u.toString());
            }
        return !b.isEmpty() ? "Listeners:" + b + ';' : "No listener.";
    }

    @Nullable
    public Entity getOwner() {
        if (cachedOwner != null && !cachedOwner.isRemoved()) return cachedOwner;
        if (this.ownerUUID != null && this.level instanceof final ServerLevel serverLevel) {
            this.cachedOwner = serverLevel.getEntity(this.ownerUUID);
        }
        return this.cachedOwner;
    }

    public void setOwner(@Nullable Entity entity) {
        if (entity == null || cachedOwner != null || ownerUUID != null) return;
        cachedOwner = entity;
        ownerUUID = entity.getUUID();
    }
}
