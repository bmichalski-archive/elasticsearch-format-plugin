#!/bin/bash

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

$DIR/kill-remove.sh

docker run \
  -it \
  -p 9200:9200 \
  -p 9300:9300 \
  -v $DIR/../target:/root/target \
  --name elasticsearch-format-plugin \
  bmichalski/elasticsearch-format-plugin \
  bash

