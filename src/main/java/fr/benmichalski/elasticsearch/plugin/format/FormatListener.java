package fr.benmichalski.elasticsearch.plugin.format;

import au.com.bytecode.opencsv.CSVWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.lang3.StringUtils;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
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

    private final String multiValuedSeparator;

    private final char multiValuedQuoteChar;

    private final Charset charset;

    public FormatListener(
        final RestChannel channel,
        final String format,
        final String[] keys,
        final char separator,
        final char quoteChar,
        final char escapeChar,
        final String lineEnd,
        final String multiValuedSeparator,
        final char multiValuedQuoteChar,
        final Charset charset
    ) {
        super(channel);

        this.format = format;
        this.keys = keys;

        this.separator = separator;
        this.quoteChar = quoteChar;
        this.escapeChar = escapeChar;
        this.lineEnd = lineEnd;
        this.multiValuedSeparator = multiValuedSeparator;
        this.multiValuedQuoteChar = multiValuedQuoteChar;
        this.charset = charset;
    }

    private RestResponse handleCsv(final SearchResponse response) throws IOException {
        final StringWriter stringWriter = new StringWriter();

        final CSVWriter csvWriter = new CSVWriter(
            stringWriter,
            this.separator,
            this.quoteChar,
            this.escapeChar,
            this.lineEnd
        );

        final ArrayList<String> stringList = new ArrayList<String>();

        Map<String, Object> sourceAsMap;
        String[] stringArr;
        final ArrayList<String> converted = new ArrayList<String>();
        boolean broken;
        List extractValueList;
        int stringListSize;

        int i = 0;

        for (SearchHit hit : response.getHits().getHits()) {
            ++i;

            sourceAsMap = hit.sourceAsMap();

            for (String key : this.keys) {
                Object extractValue = XContentMapValues.extractValue(key, sourceAsMap);

                if (null == extractValue) {
                    stringList.add("");
                } else if (XContentMapValues.isArray(extractValue) || XContentMapValues.isObject(extractValue)) {
                    //Converts simple multivalued field to string
                    if (XContentMapValues.isArray(extractValue)) {
                        extractValueList = (List)extractValue;

                        converted.clear();

                        broken = false;

                        for (Object value : extractValueList) {
                            if (null == value) {
                                converted.add(this.multiValuedQuoteChar + "" + this.multiValuedQuoteChar);
                            } if (XContentMapValues.isArray(value) | XContentMapValues.isObject(value)) {
                                //Breaking if field is not simple multivalued array
                                broken = true;
                                break;
                            } else {
                                converted.add(this.multiValuedQuoteChar + value.toString() + this.multiValuedQuoteChar);
                            }
                        }

                        if (broken) {
                            stringList.add("");
                        } else if ((stringListSize = stringList.size()) == 0) {
                            stringList.add("");
                        } else if (stringListSize == 1) {
                            stringList.add(stringList.get(0));
                        }  else {
                            stringList.add(StringUtils.join(converted, this.multiValuedSeparator));
                        }
                    } else {
                        //If content is an object, returns an empty string.
                        stringList.add("");
                    }
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
            "text/csv; charset=" + this.charset.displayName(),
            stringWriter.toString().getBytes(this.charset)
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
