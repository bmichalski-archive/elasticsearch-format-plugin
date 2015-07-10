#!/bin/bash

plugin --remove elasticsearch-format-plugin
plugin --url file:///root/target/releases/elasticsearch-format-plugin-1.0-SNAPSHOT.zip --install elasticsearch-format-plugin
service elasticsearch restart

