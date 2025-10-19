package com.stardevllc.starchat.obserable;

import com.stardevllc.config.file.FileConfig;
import com.stardevllc.starlib.observable.ChangeListener;
import com.stardevllc.starlib.observable.ObservableValue;

public class ConfigChangeListener<T> implements ChangeListener<T> {
    private final FileConfig configuration;
    private final String path;

    public ConfigChangeListener(FileConfig configuration, String path) {
        this.configuration = configuration;
        this.path = path;
    }
    
    @Override
    public void changed(ObservableValue<T> observableValue, T oldValue, T newValue) {
        configuration.set(path, newValue);
        configuration.save();
    }
}
