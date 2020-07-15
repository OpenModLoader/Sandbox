package org.sandboxpowered.sandbox.fabric.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import java.nio.file.Path;

public class Config {
    private final CommentedFileConfig config;

    public Config(Path path) {
        this.config = CommentedFileConfig.builder(path).autoreload().build();
        config.load();
    }

    public <T> ConfigValue<T> get(String path) {
        return new ConfigValue<>(config, path);
    }

    public void save() {
        config.save();
    }
}
