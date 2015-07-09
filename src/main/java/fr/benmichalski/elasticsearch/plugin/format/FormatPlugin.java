package fr.benmichalski.elasticsearch.plugin.format;

import java.util.Collection;
import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.plugins.AbstractPlugin;

public class FormatPlugin extends AbstractPlugin {
    @Override public String name() {
        return "elasticsearch-format-plugin";
    }

    @Override public String description() {
        return "A plugin to allow formating ES search results.";
    }

    @Override
    public Collection<Class<? extends Module>> modules() {
        Collection<Class<? extends Module>> modules = Lists.newArrayList();
        modules.add(FormatRestModule.class);
        return modules;
    }
}

