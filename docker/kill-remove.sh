#/bin/bash

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

ELASTICSEARCH_FORMAT_PLUGIN_EXISTS=`docker inspect --format="{{ .Id }}" elasticsearch-format-plugin 2> /dev/null`

if ! [ -z "$ELASTICSEARCH_FORMAT_PLUGIN_EXISTS" ]
then
  docker kill elasticsearch-format-plugin
  docker rm elasticsearch-format-plugin
fi

