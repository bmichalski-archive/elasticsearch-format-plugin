package fr.benmichalski.elasticsearch.plugin.format;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.rest.*;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import org.elasticsearch.rest.action.search.RestSearchAction;

public class SearchFormatRestHandler extends BaseRestHandler {

    @Inject
    public SearchFormatRestHandler(Settings settings, RestController restController, Client client) {
        super(settings, restController, client);

        restController.registerHandler(GET, "/_search_format", this);
    }

    @Override
    public void handleRequest(final RestRequest request, final RestChannel channel, final Client client) {
        SearchRequest searchRequest = RestSearchAction.parseSearchRequest(request);
        searchRequest.listenerThreaded(false);

        client.search(searchRequest, new FormatListener(channel));
    }
}
