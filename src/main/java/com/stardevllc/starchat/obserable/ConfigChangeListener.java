package com.stardevllc.starchat.obserable;

import com.stardevllc.starcore.config.Configuration;
import com.stardevllc.starlib.observable.ChangeEvent;
import com.stardevllc.starlib.observable.ChangeListener;

public class ConfigChangeListener<T> implements ChangeListener<T> {
    private final Configuration configuration;
    private final String path;

    public ConfigChangeListener(Configuration configuration, String path) {
        this.configuration = configuration;
        this.path = path;
    }

    @Override
    public void changed(ChangeEvent<T> e) {
        configuration.set(path, e.newValue());
        configuration.save();
    }
}
