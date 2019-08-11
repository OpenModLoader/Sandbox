package com.hrznstudio.sandbox.mixin.networking;

import com.hrznstudio.sandbox.api.CustomPayloadPacket;
import com.hrznstudio.sandbox.network.NetworkManager;
import com.hrznstudio.sandbox.network.Packet;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.packet.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {

    @Inject(method = "onCustomPayload", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/ThreadExecutor;)V", shift = At.Shift.AFTER), cancellable = true)
    public void onCustomPayload(CustomPayloadS2CPacket s2c, CallbackInfo info) {
        CustomPayloadPacket customPayloadPacket = (CustomPayloadPacket) s2c;
        PacketByteBuf data = null;
        try {
            Identifier channel = customPayloadPacket.channel();
            if (!channel.getNamespace().equals("minecraft")) { //Override non-vanilla packets
                Class<? extends Packet> packetClass = NetworkManager.get(channel);
                if (packetClass != null) {
                    data = customPayloadPacket.data();
                    Packet packet = packetClass.getConstructor().newInstance();
                    packet.read(data);
                    packet.apply();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (data != null) {
                data.release();
                info.cancel();
            }
        }
    }
}