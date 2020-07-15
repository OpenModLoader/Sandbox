package org.sandboxpowered.sandbox.fabric.util.wrapper;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Tickable;
import org.sandboxpowered.api.block.entity.BaseBlockEntity;
import org.sandboxpowered.api.block.entity.BlockEntity;
import org.sandboxpowered.api.util.nbt.ReadableCompoundTag;
import org.sandboxpowered.api.util.nbt.WritableCompoundTag;
import org.sandboxpowered.sandbox.fabric.util.WrappingUtil;

import javax.annotation.Nonnull;

public class BlockEntityWrapper extends net.minecraft.block.entity.BlockEntity {
    private final BlockEntity blockEntity;

    public BlockEntityWrapper(@Nonnull BlockEntity blockEntity) {
        super(WrappingUtil.convert(blockEntity.getType()));
        this.blockEntity = blockEntity;
        if (this.blockEntity instanceof BaseBlockEntity) {
            ((BaseBlockEntity) this.blockEntity).setContext(new BlockEntityCTXWrapper(this));
        }
    }

    public static BlockEntityWrapper create(BlockEntity blockEntity) {
        if (blockEntity instanceof BlockEntity.Tickable)
            return new BlockEntityWrapper.Ticking((BlockEntity.Tickable) blockEntity);
        return new BlockEntityWrapper(blockEntity);
    }

    public BlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public void fromTag(BlockState blockState, CompoundTag compoundTag_1) {
        super.fromTag(blockState, compoundTag_1);
        blockEntity.read((ReadableCompoundTag) compoundTag_1);
    }

    @Override
    public CompoundTag toTag(CompoundTag compoundTag_1) {
        super.toTag(compoundTag_1);
        blockEntity.write((WritableCompoundTag) compoundTag_1);
        return compoundTag_1;
    }

    public static class Ticking extends BlockEntityWrapper implements Tickable {
        private final BlockEntity.Tickable tickableBlockEntity;

        public Ticking(BlockEntity.Tickable blockEntity) {
            super(blockEntity);
            this.tickableBlockEntity = blockEntity;
        }

        @Override
        public void tick() {
            tickableBlockEntity.onTick();
        }
    }
}