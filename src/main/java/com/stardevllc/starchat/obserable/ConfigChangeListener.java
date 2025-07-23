package com.stardevllc.starchat.obserable;

import com.stardevllc.config.file.yaml.YamlConfig;
import com.stardevllc.starlib.observable.ChangeEvent;
import com.stardevllc.starlib.observable.ChangeListener;

import java.io.File;
import java.io.IOException;

public class ConfigChangeListener<T> implements ChangeListener<T> {
    private final YamlConfig config;
    private final File file;
    private final String path;

    public ConfigChangeListener(File file, YamlConfig config, String path) {
        this.path = path;
        this.file = file;
        this.config = config;
    }

    @Override
    public void changed(ChangeEvent<T> e) {
        config.set(path, e.newValue());
        try {
            config.save(file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
