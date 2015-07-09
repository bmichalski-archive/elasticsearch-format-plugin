package fr.benmichalski.elasticsearch.plugin.format;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.action.support.RestResponseListener;

public class FormatListener extends RestResponseListener<SearchResponse> {

    public FormatListener(RestChannel channel) {
        super(channel);
    }

    @Override
    public final RestResponse buildResponse(SearchResponse response) throws Exception {
        return new BytesRestResponse(
            response.status(),
            "text/plain",
            "foobar"
        );
    }
}