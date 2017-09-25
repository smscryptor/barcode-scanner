#!/bin/bash

GRADLE_PROPERTIES_FILE="gradle.properties"
OUTPUT_PROPS=${GRADLE_PROPERTIES_FILE}

echo "Create gradle properties file"
echo "aws_accessid=$AEVI_ARTIFACTORY_AWS_ACCESS_ID" >> ${OUTPUT_PROPS}
echo "aws_accesskey=$AEVI_ARTIFACTORY_AWS_SECRET" >> ${OUTPUT_PROPS}