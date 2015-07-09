package fr.benmichalski.elasticsearch.plugin.format;

import org.elasticsearch.common.inject.AbstractModule;

public class FormatRestModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(SearchFormatRestHandler.class).asEagerSingleton();
    }
}