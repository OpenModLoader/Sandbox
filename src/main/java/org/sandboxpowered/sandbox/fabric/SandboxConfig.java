package org.sandboxpowered.sandbox.fabric;

import org.sandboxpowered.sandbox.fabric.config.Config;
import org.sandboxpowered.sandbox.fabric.config.ConfigValue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SandboxConfig {
    public static ConfigValue<Boolean> enchantmentDecimal;
    public static ConfigValue<String> velocityKey;
    public static ConfigValue<ServerForwarding> forwarding;
    public static ConfigValue<String> addonSyncURL;
    public static ConfigValue<Boolean> disableAutoCrashSending;
    private static Config config;

    static {
        try {
            Path data = Paths.get("data");
            if (Files.notExists(data))
                Files.createDirectories(data);
            config = new Config(data.resolve("sandbox.toml"));
            enchantmentDecimal = config.get("enchantment.decimal");
            enchantmentDecimal.add(false);
            enchantmentDecimal.setComment(" Whether the Enchantment tooltip uses decimal or roman numerals");
            forwarding = config.get("server.forwarding.enable");
            forwarding.add(ServerForwarding.NONE);
            forwarding.setComment(" Use player info forwarding, 'NONE', 'BUNGEE', 'VELOCITY'");
            velocityKey = config.get("server.forwarding.key");
            velocityKey.add("KEY_HERE");
            velocityKey.setComment(" Secret key to authenticate with velocity");
            addonSyncURL = config.get("server.sync.url");
            addonSyncURL.add("https://example.com");
            addonSyncURL.setComment(" URL Prefix for the client to download server addons");
            disableAutoCrashSending = config.get("crash.auto-send");
            disableAutoCrashSending.add(false);
            disableAutoCrashSending.setComment(" Disables Sandbox automatically reporting crashes to the developers");
            config.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public enum ServerForwarding {
        NONE,
        BUNGEE,
        VELOCITY;

        public boolean isForwarding() {
            return this != NONE;
        }
    }

}