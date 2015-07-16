package fr.benmichalski.elasticsearch.plugin.format;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.rest.*;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestRequest.Method.POST;
import org.elasticsearch.rest.action.search.RestSearchAction;

public class SearchFormatRestHandler extends BaseRestHandler {

    @Inject
    public SearchFormatRestHandler(Settings settings, RestController controller, Client client) {
        super(settings, controller, client);

        controller.registerHandler(GET, "/{index}/_search_format", this);
        controller.registerHandler(POST, "/{index}/_search_format", this);
        controller.registerHandler(GET, "/{index}/{type}/_search_format", this);
        controller.registerHandler(POST, "/{index}/{type}/_search_format", this);
        controller.registerHandler(GET, "/_search_format", this);
    }

    @Override
    public void handleRequest(final RestRequest request, final RestChannel channel, final Client client) {
        SearchRequest searchRequest = RestSearchAction.parseSearchRequest(request);
        searchRequest.listenerThreaded(false);

        client.search(
            searchRequest,
            new FormatListener(
                channel,
                request.param("format", "csv"),
                request.paramAsStringArray("keys", null)
            )
        );
    }
}
