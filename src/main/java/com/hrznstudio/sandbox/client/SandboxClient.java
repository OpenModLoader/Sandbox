package com.hrznstudio.sandbox.client;

import com.hrznstudio.sandbox.SandboxCommon;
import com.hrznstudio.sandbox.api.game.GameMode;
import com.hrznstudio.sandbox.api.util.Mono;
import com.hrznstudio.sandbox.api.util.Side;
import com.hrznstudio.sandbox.client.overlay.LoadingOverlay;
import com.hrznstudio.sandbox.event.EventDispatcher;
import com.hrznstudio.sandbox.loader.SandboxLoader;
import com.hrznstudio.sandbox.server.SandboxServer;
import com.hrznstudio.sandbox.util.Log;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.ServerEntry;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class SandboxClient extends SandboxCommon {
    public static SandboxClient INSTANCE;
    public SandboxLoader loader;

    private SandboxClient() {
        INSTANCE = this;
    }

    public static SandboxClient constructAndSetup() {
        SandboxClient client = new SandboxClient();
        client.setup();
        return client;
    }

    @Override
    public Side getSide() {
        return Side.CLIENT;
    }

    @Override
    protected void setup() {
        //Init client engine
        Log.info("Setting up Clientside Sandbox environment");
        Mono<GameMode> mono = server.getGameMode();
        DiscordRichPresence.Builder builder;
        builder = new DiscordRichPresence.Builder("In Game");
        builder.setStartTimestamps(System.currentTimeMillis() / 1000);
        if (MinecraftClient.getInstance().getCurrentServerEntry() != null) {
            ServerEntry entry = MinecraftClient.getInstance().getCurrentServerEntry();
            String[] str = Formatting.strip(entry.playerCountLabel).split("/");
            if (str.length == 2) {
                int i = Integer.parseInt(str[0]);
                int i1 = Integer.parseInt(str[1]);
                builder.setDetails("In Multiplayer");
                builder.setParty(Base64.encodeBase64String(entry.address.getBytes()), i, i1);
            }
        } else {
            builder.setDetails("In Singleplayer");
        }

        mono.ifPresent(
                mode -> builder.setBigImage(mode.getRichImage().orElse("logo"), String.format("Playing %s", mode.getDisplayName())),
                () -> builder.setBigImage("logo", "Playing Sandbox")
        );

        DiscordRPC.discordUpdatePresence(builder.build());
    }

    @Override
    public void shutdown() {
        INSTANCE = null;
        if (SandboxServer.INSTANCE == null)
            EventDispatcher.clear();
    }

    public void open(String prefix, List<Pair<String, String>> addons) {
        MinecraftClient.getInstance().setOverlay(new LoadingOverlay(
                MinecraftClient.getInstance(),
                prefix,
                addons
        ));
    }

    public void load(List<Path> addons) {
        loader = new SandboxLoader(this, addons);
        try {
            loader.load(SandboxServer.INSTANCE == null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MinecraftClient.getInstance().worldRenderer.reload();
        MinecraftClient.getInstance().reloadResourcesConcurrently();
    }
}