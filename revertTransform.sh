#!/bin/bash
find . -name "*.bak" -print -exec bash -c 'mv "$1" "${1%.bak}".xml' - '{}' \;
