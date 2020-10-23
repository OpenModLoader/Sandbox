package org.sandboxpowered.sandbox.fabric.mixin.fabric.client;

import net.minecraft.client.render.GameRenderer;
import org.sandboxpowered.sandbox.fabric.client.PanoramaHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @Inject(method = "getFov",
            at = @At("HEAD"),
            cancellable = true
    )
    public void getFov(CallbackInfoReturnable<Double> info) {
        if (PanoramaHandler.INSTANCE.isTakingPanorama())
            info.setReturnValue(90.0D);
    }
}