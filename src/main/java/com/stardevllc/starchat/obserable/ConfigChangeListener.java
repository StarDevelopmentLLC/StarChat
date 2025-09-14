package com.stardevllc.starchat.obserable;

import com.stardevllc.config.file.FileConfig;
import com.stardevllc.starlib.observable.ChangeEvent;
import com.stardevllc.starlib.observable.ChangeListener;

public class ConfigChangeListener<T> implements ChangeListener<T> {
    private final FileConfig configuration;
    private final String path;

    public ConfigChangeListener(FileConfig configuration, String path) {
        this.configuration = configuration;
        this.path = path;
    }

    @Override
    public void changed(ChangeEvent<T> e) {
        configuration.set(path, e.newValue());
        configuration.save();
    }
}
