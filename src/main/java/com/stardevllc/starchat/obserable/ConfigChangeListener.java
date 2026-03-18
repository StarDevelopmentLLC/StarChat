package com.stardevllc.starchat.obserable;

import com.stardevllc.config.file.FileConfig;
import com.stardevllc.starlib.values.ObservableValue;
import com.stardevllc.starlib.values.observable.listener.ChangeListener;

public class ConfigChangeListener<T> implements ChangeListener<T> {
    private final FileConfig configuration;
    private final String path;

    public ConfigChangeListener(FileConfig configuration, String path) {
        this.configuration = configuration;
        this.path = path;
    }
    
    @Override
    public void onChange(ObservableValue<T> observableValue, T o, T n) {
        configuration.set(path, n);
        configuration.save();
    }
}