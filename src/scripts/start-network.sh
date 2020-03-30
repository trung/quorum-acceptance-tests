#!/bin/bash

set -e

if [[ $# -ne 2 ]]; then
  echo "Usage: $0 <network_tf_dir> <network_output_dir>"
  exit 1
fi

network_tf_dir=$1
network_output_dir=$2

pushd $network_tf_dir > /dev/null

echo "Terraform Init"
terraform init
echo "Terraform Apply"
terraform apply --auto-approve -var output_dir=$network_output_dir -var network_name=$(basename $network_tf_dir)

popd > /dev/null