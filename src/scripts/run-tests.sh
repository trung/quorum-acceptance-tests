#!/bin/bash

set -e

if [[ $# -lt 2 ]]; then
  echo "Usage: $0 <network_name> <network_output_dir> [tags] [runId]"
  exit 1
fi

spring_profile=$1
spring_config_dir=$2
tags=$3
run_id=$4

effective_tags=${tags:-basic}
echo "Running tests with:"
echo "   tags=$effective_tags"

ENV_DIR="run-${run_id:-local}"

mkdir -p env/${ENV_DIR}
SPRING_PROFILES_ACTIVE=$spring_profile \
SPRING_CONFIG_ADDITIONALLOCATION=file:$spring_config_dir \
mvn clean test -q -Denv=${ENV_DIR} -Dtags="$effective_tags"