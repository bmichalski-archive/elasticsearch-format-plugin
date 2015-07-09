package org.elasticsearch.plugin.example;

import org.elasticsearch.plugins.AbstractPlugin;

public class ExamplePlugin extends AbstractPlugin {
    @Override public String name() {
        return "example-plugin";
    }

    @Override public String description() {
        return "Example Plugin Description";
    }
}

