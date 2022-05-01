package committee.nova.firesafety.common.block.blockEntity.impl;

import committee.nova.firesafety.common.util.PlayerHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

import static committee.nova.firesafety.common.block.impl.ExtinguisherBlock.WATERED;
import static committee.nova.firesafety.common.block.reference.BlockReference.EXTINGUISHER;
import static committee.nova.firesafety.common.block.reference.BlockReference.getRegisteredBlockEntityType;

@ParametersAreNonnullByDefault
public class ExtinguisherBlockEntity extends FireAlarmBlockEntity {
    private final int maxWaterStorage = 1000;
    private int waterStorage = maxWaterStorage;

    public ExtinguisherBlockEntity(BlockPos pos, BlockState state) {
        super(getRegisteredBlockEntityType(EXTINGUISHER), pos, state);
    }

    public boolean tickServer(BlockState state) {
        final boolean needExtinguish = super.tickServer(state);
        if (waterStorage < 0) waterStorage = 0;
        assert level != null;
        if (waterStorage == 0) {
            level.setBlockAndUpdate(worldPosition, state.setValue(WATERED, false));
        }
        level.setBlockAndUpdate(worldPosition, state.setValue(WATERED, true));
        if (!needExtinguish) return true;
        tryExtinguish();
        return true;
    }

    @Override
    public void load(CompoundTag tag) {
        tag.putInt("water", waterStorage);
        super.load(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        waterStorage = tag.getInt("water");
        super.saveAdditional(tag);
    }

    private void tryExtinguish() {
        assert level != null;
        if (level.getDayTime() % 50 != 0) return;
        final int remain = waterStorage;
        if (remain >= 10) {
            waterStorage -= 10;
            extinguish();
            return;
        }
        waterStorage = 0;
        extinguish(remain);
        toListeningPlayers(level, player -> PlayerHandler.playSoundForThisPlayer(player, SoundEvents.BUCKET_FILL, 1F, 1F));
        toListeningPlayers(level, player -> PlayerHandler.notifyServerPlayer(player, new TranslatableComponent("msg.firesafety.insufficient_water")));
    }

    private void extinguish(int amount) {
        if (amount == 0) return;
        assert level != null;
        final Iterable<BlockPos> posList = BlockPos.betweenClosed(worldPosition.offset(5, 0, 5), worldPosition.offset(-5, -10, -5));
        final Random r = level.random;
        for (final BlockPos p : posList) {
            if (level.getBlockState(p).is(Blocks.FIRE) && r.nextInt(amount + 1) > 15) {
                level.setBlockAndUpdate(p, Blocks.AIR.defaultBlockState());
                level.playSound(null, p, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1F, 1F);
            }
        }
    }

    private void extinguish() {
        extinguish(50);
    }

    public int getWaterStorage() {
        return waterStorage;
    }

    public void setWaterStorage(int i) {
        waterStorage = Math.min(i, maxWaterStorage);
    }

    public int getMaxWaterStorage() {
        return maxWaterStorage;
    }
}
