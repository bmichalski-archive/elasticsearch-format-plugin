package fr.benmichalski.elasticsearch.plugin.format;

import au.com.bytecode.opencsv.CSVWriter;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.*;
import org.elasticsearch.rest.action.support.RestActions;
import org.elasticsearch.search.Scroll;

import static org.elasticsearch.common.unit.TimeValue.parseTimeValue;
import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestRequest.Method.POST;

public class ScrollSearchFormatRestHandler extends BaseRestHandler {

    @Inject
    public ScrollSearchFormatRestHandler(Settings settings, RestController controller, Client client) {
        super(settings, controller, client);

        controller.registerHandler(GET, "/_search_format/scroll", this);
        controller.registerHandler(POST, "/_search_format/scroll", this);
        controller.registerHandler(GET, "/_search_format/scroll/{scroll_id}", this);
        controller.registerHandler(POST, "/_search_format/scroll/{scroll_id}", this);
    }

    @Override
    public void handleRequest(final RestRequest request, final RestChannel channel, final Client client) {
        String scrollId = request.param("scroll_id");

        if (scrollId == null) {
            scrollId = RestActions.getRestContent(request).toUtf8();
        }

        SearchScrollRequest searchScrollRequest = new SearchScrollRequest(scrollId);
        searchScrollRequest.listenerThreaded(false);

        String scroll = request.param("scroll");

        if (scroll != null) {
            searchScrollRequest.scroll(new Scroll(parseTimeValue(scroll, null)));
        }

        client.searchScroll(
            searchScrollRequest,
            new FormatListener(
                channel,
                request.param("format", "csv"),
                request.paramAsStringArray("keys", new String[0]),
                request.param("separator", String.valueOf(CSVWriter.DEFAULT_SEPARATOR)).charAt(0),
                request.param("quoteChar", String.valueOf(CSVWriter.DEFAULT_QUOTE_CHARACTER)).charAt(0),
                request.param("escapeChar", String.valueOf(CSVWriter.DEFAULT_ESCAPE_CHARACTER)).charAt(0),
                request.param("lineEnd", CSVWriter.DEFAULT_LINE_END),
                request.param("multiValuedSeparator", " | "),
                request.param("multiValuedQuoteChar", "\"").charAt(0)
            )
        );
    }
}
