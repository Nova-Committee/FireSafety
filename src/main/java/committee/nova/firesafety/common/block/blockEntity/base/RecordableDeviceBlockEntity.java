package committee.nova.firesafety.common.block.blockEntity.base;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Function;

@ParametersAreNonnullByDefault
public abstract class RecordableDeviceBlockEntity extends BlockEntity {
    public final HashMap<UUID, Boolean> notifies = new HashMap<>();

    public RecordableDeviceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void load(CompoundTag tag) {
        final ListTag listeners = tag.getList("listeners", Tag.TAG_COMPOUND);
        synchronized (notifies) {
            notifies.clear();
            if (listeners.isEmpty()) return;
            final int lSize = listeners.size();
            for (int i = 0; i < lSize; i++) {
                final CompoundTag s = listeners.getCompound(i);
                notifies.put(s.getUUID("uuid"), s.getBoolean("on"));
            }
        }
        super.load(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (notifies.isEmpty()) return;
        final ListTag listeners = new ListTag();
        for (final UUID uuid : notifies.keySet()) {
            final CompoundTag u = new CompoundTag();
            u.putUUID("uuid", uuid);
            u.putBoolean("on", notifies.get(uuid));
            listeners.add(u);
        }
        tag.put("listeners", listeners);
    }

    public boolean handleListener(Player player) {
        final UUID u = player.getUUID();
        if (!notifies.containsKey(u) || !notifies.get(u)) {
            notifies.put(u, true);
            return true;
        }
        notifies.remove(u);
        return false;
    }

    public void toListeningPlayers(Level level, Function<Player, ?> action) {
        final MinecraftServer server = level.getServer();
        if (server == null) return;
        final PlayerList list = server.getPlayerList();
        for (final UUID u : notifies.keySet()) {
            if (!notifies.containsKey(u) || !notifies.get(u)) continue;
            final Player p = list.getPlayer(u);
            if (p == null) continue;
            action.apply(p);
        }
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder();
        for (final UUID u : notifies.keySet())
            if (notifies.get(u)) {
                b.append(" ");
                b.append(u.toString());
            }
        return !b.isEmpty() ? "listeners:" + b + ';' : "No listener.";
    }
}
