package com.hrznstudio.sandbox.mixin.client;

import com.hrznstudio.sandbox.Sandbox;
import com.hrznstudio.sandbox.client.SandboxClient;
import com.hrznstudio.sandbox.SandboxHooks;
import com.hrznstudio.sandbox.resources.AddonResourcePack;
import com.hrznstudio.sandbox.resources.SandboxResourceCreator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.ClientResourcePackContainer;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackContainerManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

    @Shadow
    @Final
    private ResourcePackContainerManager<ClientResourcePackContainer> resourcePackContainerManager;

    private void addonResourcePackModifications(List<ResourcePack> packs) {
        if(SandboxClient.INSTANCE!=null) {
            SandboxClient.INSTANCE.loadedAddons.forEach(info -> {
                packs.add(new AddonResourcePack(info));
            });
        }
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void constructor(CallbackInfo info) {
        this.resourcePackContainerManager.addCreator(new SandboxResourceCreator());
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;", ordinal = 0, shift = At.Shift.BY, by = -2), locals = LocalCapture.CAPTURE_FAILHARD)
    public void initResources(CallbackInfo info, List<ResourcePack> list) {
        addonResourcePackModifications(list);
    }

    @Inject(method = "startIntegratedServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/integrated/IntegratedServer;start()V"))
    public void clientSetup(CallbackInfo info) {
        SandboxClient.constructAndSetup();
    }

    @Inject(method = "init", at = @At("HEAD"))
    public void initGlobal(CallbackInfo info) {
        SandboxHooks.setupGlobal();
    }

    @Inject(method = "close", at = @At("HEAD"))
    public void shutdownGlobal(CallbackInfo info) {
        SandboxHooks.shutdownGlobal();
    }

    @Inject(method = "reloadResources", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ReloadableResourceManager;beginMonitoredReload(Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Ljava/util/List;)Lnet/minecraft/resource/ResourceReloadMonitor;", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    public void reloadResources(CallbackInfoReturnable<CompletableFuture> info, CompletableFuture<java.lang.Void> cf, List<ResourcePack> list) {
        addonResourcePackModifications(list);
    }

    @ModifyVariable(method = "openScreen", at = @At("HEAD"), ordinal = 0)
    public Screen openScreen(Screen screen) {
        return SandboxHooks.openScreen(screen);
    }
}