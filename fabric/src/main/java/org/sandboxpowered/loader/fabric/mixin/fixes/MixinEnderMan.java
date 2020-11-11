package org.sandboxpowered.loader.fabric.mixin.fixes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderMan.class)
public abstract class MixinEnderMan extends Monster implements NeutralMob {
    public MixinEnderMan(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * Fixes <a href="https://bugs.mojang.com/browse/MC-189565">MC-189565</a>
     */
    @Inject(method = "readAdditionalSaveData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/EnderMan;setCarriedBlock(Lnet/minecraft/world/level/block/state/BlockState;)V", shift = At.Shift.AFTER), cancellable = true)
    private void worldCheckAngerFromTag(CompoundTag tag, CallbackInfo ci) {
        if (!this.level.isClientSide) {
            this.readPersistentAngerSaveData((ServerLevel) level, tag);
        }

        ci.cancel();
    }
}
