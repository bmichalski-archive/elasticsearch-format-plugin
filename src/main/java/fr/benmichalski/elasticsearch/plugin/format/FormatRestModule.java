package fr.benmichalski.elasticsearch.plugin.format;

import org.elasticsearch.common.inject.AbstractModule;

public class FormatRestModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ScrollSearchFormatRestHandler.class).asEagerSingleton();
        bind(SearchFormatRestHandler.class).asEagerSingleton();
    }
}
