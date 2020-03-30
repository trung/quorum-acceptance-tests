#!/bin/bash

set -e

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 <network_dir>"
  exit 1
fi

network_dir=$1

pushd $network_dir > /dev/null

echo "Terraform Destroy"
terraform destroy --auto-approve

popd > /dev/null