A Terraform module that uses `terraform-provider-docker` to create containers in Docker

This is best used along with the main module and submodule `docker-helper` to simplify the inputs.

## Environment Variables

* `ALWAYS_REFRESH`: when `geth` container starts, it always replaces its data folder from host data folder.
* `ADDITIONAL_GETH_ARGS`: string value being appended to `geth`