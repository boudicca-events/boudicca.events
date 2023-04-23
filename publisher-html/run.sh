#!/bin/bash

if [[ -z "${BASE_URL}" ]]; then
  echo "no base url set"
else
  echo "replacing base url to ${BASE_URL}"
  sed -i "s|##REPLACEME##|${BASE_URL}|g" /usr/local/apache2/htdocs/main.js
fi

exec httpd-foreground