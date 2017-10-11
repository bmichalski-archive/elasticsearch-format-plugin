# elasticsearch-format-plugin

This plugin allows to format the output of REST search responses.

The REST endpoints are suffixed _format, equivalent to the REST endpoint _search except that the result set can be formated.

Currently supported output formats are:
* csv

### Compatibility
This plugin has been tested and is supposed to work with Elasticsearch v1.6.

### Minimal curl example 

	curl -XPUT localhost:9200/_search_format?format=csv&keys=foo,bar

### Available options

* format, a string
  * currently, only "csv" (with no quotes) is supported
  * default value is "csv"
* keys, a comma separated list of keys to include in the output
  * example "foo,bar"
  * default value is an empty string list
* separator, a single character
  * default value is ,
* quoteChar, a single character
  * default value is "
* escapeChar, a single character
  * default value is "
* lineEnd, a string
  * default value is "\n"
* multiValuedSeparator, a string
  * default value is " | "
* multiValuedQuoteChar, a single character
  * default value is "
* charset, a string
  * default value is "UTF-8"
  * supported charset defined in http://docs.oracle.com/javase/7/docs/technotes/guides/intl/encoding.doc.html, in first column "Canonical Name for java.nio API"
