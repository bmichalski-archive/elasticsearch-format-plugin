package fr.benmichalski.elasticsearch.plugin.format;

import au.com.bytecode.opencsv.CSVWriter;
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

    public FormatListener(RestChannel channel) {
        super(channel);
    }

    @Override
    public final RestResponse buildResponse(SearchResponse response) throws Exception {
        try {
            XContentBuilder builder = this.channel.newBuilder();

            OutputStream stream = builder.stream();

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(stream);

            CSVWriter csvWriter = new CSVWriter(outputStreamWriter);

            int i = 0;

            logger.info("Total hits: " + (new Long(response.getHits().totalHits())).toString());

            ArrayList<String> stringList = new ArrayList<String>();

            for (SearchHit hit : response.getHits().getHits()) {
                ++i;

                Map<String, Object> sourceAsMap = hit.sourceAsMap();

                for (Map.Entry<String, Object> field : sourceAsMap.entrySet()) {
                    String key = field.getKey();

                    Object extractValue = XContentMapValues.extractValue(key, sourceAsMap);

                    if (null == extractValue) {
                        stringList.add("");
                    } else if (XContentMapValues.isArray(extractValue) || XContentMapValues.isObject(extractValue)) {
                        stringList.add(""); //TODO
                    } else {
                        stringList.add(extractValue.toString());
                    }
                }

                String[] stringArr = new String[stringList.size()];
                stringArr = stringList.toArray(stringArr);

                stringList.clear();

                csvWriter.writeNext(stringArr);

                if (i % 1000 == 0) {
                    csvWriter.flush();
                }
            }

            //Final flush
            csvWriter.flush();

            BytesRestResponse bytesRestResponse = new BytesRestResponse(
                response.status(),
                builder
            );

            String scrollId = response.getScrollId();

            if (null != scrollId) {
                bytesRestResponse.addHeader("X-SCROLL-ID", response.getScrollId());
            }

            return bytesRestResponse;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            return new BytesRestResponse(channel, RestStatus.INTERNAL_SERVER_ERROR, t);
        }
    }
}