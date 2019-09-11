package com.hrznstudio.sandbox.mixin.impl.block;

import com.hrznstudio.sandbox.api.SandboxInternal;
import com.hrznstudio.sandbox.api.block.IBlock;
import com.hrznstudio.sandbox.api.block.Material;
import com.hrznstudio.sandbox.api.block.entity.IBlockEntity;
import com.hrznstudio.sandbox.api.component.Component;
import com.hrznstudio.sandbox.api.component.Components;
import com.hrznstudio.sandbox.api.entity.IEntity;
import com.hrznstudio.sandbox.api.entity.player.Hand;
import com.hrznstudio.sandbox.api.entity.player.Player;
import com.hrznstudio.sandbox.api.item.IItem;
import com.hrznstudio.sandbox.api.item.ItemStack;
import com.hrznstudio.sandbox.api.state.BlockState;
import com.hrznstudio.sandbox.api.util.Direction;
import com.hrznstudio.sandbox.api.util.InteractionResult;
import com.hrznstudio.sandbox.api.util.Mono;
import com.hrznstudio.sandbox.api.util.math.Position;
import com.hrznstudio.sandbox.api.util.math.Vec3f;
import com.hrznstudio.sandbox.api.world.World;
import com.hrznstudio.sandbox.api.world.WorldReader;
import com.hrznstudio.sandbox.util.WrappingUtil;
import com.hrznstudio.sandbox.util.wrapper.SidedRespective;
import com.hrznstudio.sandbox.util.wrapper.StateFactoryImpl;
import com.hrznstudio.sandbox.util.wrapper.V2SInventory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.Blocks;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.state.StateFactory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(net.minecraft.block.Block.class)
@Implements(@Interface(iface = IBlock.class, prefix = "sbx$", remap = Interface.Remap.NONE))
@Unique
public abstract class MixinBlock implements SandboxInternal.StateFactoryHolder {
    @Shadow
    @Final
    protected StateFactory<Block, net.minecraft.block.BlockState> stateFactory;
    private com.hrznstudio.sandbox.api.state.StateFactory<IBlock, BlockState> sandboxFactory;

    @Shadow
    public abstract boolean hasBlockEntity();

    @Shadow
    public abstract void onPlaced(net.minecraft.world.World world_1, BlockPos blockPos_1, net.minecraft.block.BlockState blockState_1, @Nullable LivingEntity livingEntity_1, net.minecraft.item.ItemStack itemStack_1);

    @Shadow
    public abstract net.minecraft.item.Item asItem();

    @Shadow
    public abstract net.minecraft.block.BlockState getDefaultState();

    @Shadow
    @Deprecated
    public abstract boolean isAir(net.minecraft.block.BlockState blockState_1);

    @Shadow
    public abstract void onBroken(IWorld iWorld_1, BlockPos blockPos_1, net.minecraft.block.BlockState blockState_1);

    @Shadow
    public abstract net.minecraft.item.ItemStack getPickStack(BlockView blockView_1, BlockPos blockPos_1, net.minecraft.block.BlockState blockState_1);

    @Inject(method = "<init>", at = @At("RETURN"))
    public void constructor(Block.Settings settings, CallbackInfo info) {
        sandboxFactory = new StateFactoryImpl<>(this.stateFactory, b -> (IBlock) b, s -> (com.hrznstudio.sandbox.api.state.BlockState) s);
        ((SandboxInternal.StateFactory) this.stateFactory).setSboxFactory(sandboxFactory);
    }

    public IBlock.Settings sbx$getSettings() {
        return new IBlock.Settings(Material.AIR);
    }

    @Override
    public com.hrznstudio.sandbox.api.state.StateFactory getSandboxStateFactory() {
        return sandboxFactory;
    }

    public boolean sbx$isAir(BlockState state) {
        return this.isAir(WrappingUtil.convert(state));
    }

    public BlockState sbx$getBaseState() {
        return (BlockState) this.getDefaultState();
    }

    public InteractionResult sbx$onBlockUsed(World world, Position pos, BlockState state, Player player, Hand hand, Direction side, Vec3f hit) {
        return InteractionResult.IGNORE;
    }

    public InteractionResult sbx$onBlockClicked(World world, Position pos, BlockState state, Player player) {
        return InteractionResult.IGNORE;
    }

    public IItem sbx$asItem() {
        return (IItem) asItem();
    }

    public void sbx$onBlockPlaced(World world, Position position, BlockState state, IEntity entity, ItemStack itemStack) {
        this.onPlaced(
                WrappingUtil.convert(world),
                WrappingUtil.convert(position),
                WrappingUtil.convert(state),
                null,
                WrappingUtil.convert(itemStack)
        );
    }

    public void sbx$onBlockBroken(World world, Position position, BlockState state) {
        this.onBroken((net.minecraft.world.World) world, (BlockPos) position, (net.minecraft.block.BlockState) state);
    }

    public boolean sbx$hasBlockEntity() {
        return this.hasBlockEntity();
    }

    public <X> Mono<X> sbx$getComponent(WorldReader reader, Position position, BlockState state, Component<X> component, Mono<Direction> side) {
        if (component == Components.INVENTORY_COMPONENT) {
            if (this instanceof InventoryProvider) {
                SidedInventory inventory = ((InventoryProvider) this).getInventory((net.minecraft.block.BlockState) state, (IWorld) reader, (BlockPos) position);
                if (side.isPresent())
                    return Mono.of(new SidedRespective(inventory, side.get())).cast();
                return Mono.of(new V2SInventory(inventory)).cast();
            }
            BlockEntity entity = ((BlockView) reader).getBlockEntity((BlockPos) position);
            if (entity instanceof Inventory) {
                if (side.isPresent() && entity instanceof SidedInventory)
                    return Mono.of(new SidedRespective((SidedInventory) entity, side.get())).cast();
                return Mono.of(new V2SInventory((Inventory) entity)).cast();
            }
        }
        return Mono.empty();
    }

    @Nullable
    public IBlockEntity sbx$createBlockEntity(WorldReader reader) {
        if (hasBlockEntity())
            return (IBlockEntity) ((BlockEntityProvider) this).createBlockEntity(WrappingUtil.convert(reader));
        return null;
    }

    public boolean sbx$isNaturalStone() {
        return (Object) this == Blocks.STONE || (Object) this == Blocks.GRANITE || (Object) this == Blocks.DIORITE || (Object) this == Blocks.ANDESITE;
    }

    public boolean sbx$isNaturalDirt() {
        return (Object) this == Blocks.DIRT || (Object) this == Blocks.COARSE_DIRT || (Object) this == Blocks.PODZOL;
    }

    public ItemStack sbx$getPickStack(WorldReader reader, Position position, BlockState state) {
        return WrappingUtil.cast(getPickStack(
                (BlockView) reader,
                (BlockPos) position,
                (net.minecraft.block.BlockState) state
        ), ItemStack.class);
    }
}