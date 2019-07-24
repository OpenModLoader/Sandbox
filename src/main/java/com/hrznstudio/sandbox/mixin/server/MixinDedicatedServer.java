package com.hrznstudio.sandbox.mixin.server;

import com.hrznstudio.sandbox.mixin.MixinMinecraftServer;
import com.hrznstudio.sandbox.server.SandboxServer;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.script.ScriptException;

@Mixin(value = MinecraftDedicatedServer.class)
public class MixinDedicatedServer extends MixinMinecraftServer {
    @Inject(method = "setupServer",
            at = @At(value = "INVOKE",
                    target = "net/minecraft/server/dedicated/MinecraftDedicatedServer.setPlayerManager(Lnet/minecraft/server/PlayerManager;)V",
                    shift = At.Shift.AFTER),
            cancellable = true
    )
    public void setupServer(CallbackInfoReturnable<Boolean> info) throws ScriptException {
        SandboxServer.constructAndSetup(false);
    }
}