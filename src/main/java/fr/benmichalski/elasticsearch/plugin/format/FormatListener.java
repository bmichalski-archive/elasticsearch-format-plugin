package fr.benmichalski.elasticsearch.plugin.format;

import au.com.bytecode.opencsv.CSVWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Map;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.support.XContentMapValues;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.rest.action.support.RestResponseListener;
import org.elasticsearch.search.SearchHit;

public class FormatListener extends RestResponseListener<SearchResponse> {

    private final static ESLogger logger = ESLoggerFactory.getLogger("format");

    private final String format;

    private final String[] keys;

    private final char separator;

    private final char quoteChar;

    private final char escapeChar;

    private final String lineEnd;

    public FormatListener(
        final RestChannel channel,
        final String format,
        final String[] keys,
        final char separator,
        final char quoteChar,
        final char escapeChar,
        final String lineEnd
    ) {
        super(channel);

        this.format = format;
        this.keys = keys;

        this.separator = separator;
        this.quoteChar = quoteChar;
        this.escapeChar = escapeChar;
        this.lineEnd = lineEnd;
    }

    private RestResponse handleCsv(final SearchResponse response) throws IOException {
        final XContentBuilder builder = this.channel.newBuilder();

        final OutputStream stream = builder.stream();

        final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(stream);

        final CSVWriter csvWriter = new CSVWriter(
            outputStreamWriter,
            this.separator,
            this.quoteChar,
            this.escapeChar,
            this.lineEnd
        );

        final ArrayList<String> stringList = new ArrayList<String>();

        Map<String, Object> sourceAsMap;
        String[] stringArr;
        int i = 0;

        for (SearchHit hit : response.getHits().getHits()) {
            ++i;

            sourceAsMap = hit.sourceAsMap();

            for (String key : this.keys) {
                Object extractValue = XContentMapValues.extractValue(key, sourceAsMap);

                if (null == extractValue) {
                    stringList.add("");
                } else if (XContentMapValues.isArray(extractValue) || XContentMapValues.isObject(extractValue)) {
                    stringList.add(""); //TODO
                } else {
                    stringList.add(extractValue.toString());
                }
            }

            stringArr = new String[stringList.size()];
            stringArr = stringList.toArray(stringArr);

            stringList.clear();

            csvWriter.writeNext(stringArr);

            if (i % 1000 == 0) {
                csvWriter.flush();
            }
        }

        //Final flush
        csvWriter.flush();

        final BytesRestResponse bytesRestResponse = new BytesRestResponse(
            response.status(),
            builder
        );

        final String scrollId = response.getScrollId();

        if (null != scrollId) {
            bytesRestResponse.addHeader("scroll-id", response.getScrollId());
        }

        return bytesRestResponse;
    }

    @Override
    public final RestResponse buildResponse(SearchResponse response) throws Exception {
        try {
            if ("csv".equals(this.format)) {
                this.handleCsv(response);
            } else {
                throw new Exception(
                    "Unexpected format \""
                    + this.format +"\". "
                    + "The only currently supported format is \"csv\"."
                );
            }

            return this.handleCsv(response);
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);

            return new BytesRestResponse(channel, RestStatus.INTERNAL_SERVER_ERROR, t);
        }
    }
}