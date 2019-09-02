package com.hrznstudio.sandbox.mixin.impl.client;

import com.hrznstudio.sandbox.api.fluid.IFluid;
import com.hrznstudio.sandbox.util.WrappingUtil;
import com.hrznstudio.sandbox.util.wrapper.FluidWrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.block.FluidRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.fluid.FluidState;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ExtendedBlockView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.LinkedHashMap;
import java.util.Map;

@Mixin(FluidRenderer.class)
public abstract class MixinFluidRenderer {

    private Map<IFluid, Sprite[]> spriteMap = new LinkedHashMap<>();

    @Shadow
    @Final
    private Sprite[] waterSprites;
    private ThreadLocal<FluidState> stateThreadLocal = new ThreadLocal<>();

    @Inject(at = @At("RETURN"), method = "onResourceReload")
    public void onResourceReloadReturn(CallbackInfo info) {
        SpriteAtlasTexture spriteAtlasTexture_1 = MinecraftClient.getInstance().getSpriteAtlas();
        spriteMap.clear();
        Registry.FLUID.forEach(fluid -> {
            if (fluid instanceof FluidWrapper) {
                Sprite[] sprites = new Sprite[2];
                sprites[0] = spriteAtlasTexture_1.getSprite(WrappingUtil.convert(((FluidWrapper) fluid).fluid.getTexturePath(false)));
                sprites[1] = spriteAtlasTexture_1.getSprite(WrappingUtil.convert(((FluidWrapper) fluid).fluid.getTexturePath(true)));
                spriteMap.put(((FluidWrapper) fluid).fluid, sprites);
            }
        });
    }

    @Inject(at = @At("HEAD"), method = "tesselate", cancellable = true)
    public void tesselate(ExtendedBlockView view, BlockPos pos, BufferBuilder bufferBuilder, FluidState state, CallbackInfoReturnable<Boolean> info) {
        stateThreadLocal.set(state);
    }

    @ModifyVariable(method = "tesselate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/color/world/BiomeColors;getWaterColor(Lnet/minecraft/world/ExtendedBlockView;Lnet/minecraft/util/math/BlockPos;)I"))
    public Sprite[] sprites(Sprite[] sprites) {
        FluidState state = stateThreadLocal.get();
        if (state.getFluid() instanceof FluidWrapper) {
            return spriteMap.getOrDefault(((FluidWrapper) state.getFluid()).fluid, waterSprites);
        }
        return sprites;
    }

    @ModifyVariable(at = @At(value = "INVOKE", target = "net/minecraft/client/render/block/FluidRenderer.isSameFluid(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;Lnet/minecraft/fluid/FluidState;)Z"), method = "tesselate", ordinal = 0)
    public boolean modLavaCheck(boolean chk) {
        return chk || (stateThreadLocal.get() != null && !stateThreadLocal.get().matches(FluidTags.WATER));
    }

    @Inject(at = @At("RETURN"), method = "tesselate")
    public void tesselateReturn(ExtendedBlockView view, BlockPos pos, BufferBuilder bufferBuilder, FluidState state, CallbackInfoReturnable<Boolean> info) {
        stateThreadLocal.remove();
    }

    @ModifyVariable(at = @At(value = "CONSTANT", args = "intValue=16", ordinal = 0, shift = At.Shift.BEFORE), method = "tesselate", ordinal = 0)
    public int modTintColor(int chk) {
        return stateThreadLocal.get() == null ? chk : -1;
    }
}