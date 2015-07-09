#!/bin/bash

plugin --remove example-plugin
plugin --url file:///root/target/releases/example-plugin-1.0-SNAPSHOT.zip --install example-plugin
service elasticsearch restart

