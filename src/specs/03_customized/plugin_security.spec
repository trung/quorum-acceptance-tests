# Pluggable Architecture with security plugin

 Tags: networks/plugins, plugin-security

* Configure the authorization server to grant `"Client 1"` access to scopes `"rpc://eth_*,rpc://rpc_modules"` in `"Node1,Node2"`
* Configure the authorization server to grant `"Operator A"` access to scopes `"rpc://admin_peer"` in `"Node1,Node2"`

## Clients are authorized to access APIs based on specific scope granted

* `"Client 1"` is "authorized" to invoke:
    | api           | node    |
    |---------------|---------|
    | `rpc_modules` | `Node1` |
    | `rpc_modules` | `Node2` |
* `"Client 1"` is "not authorized" to invoke:
    | api           | node    |
    |---------------|---------|
    | `rpc_modules` | `Node3` |
    | `rpc_modules` | `Node4` |
* `"Operator A"` is "authorized" to invoke:
    | api          | node    |
    |--------------|---------|
    | `admin_peer` | `Node1` |
    | `admin_peer` | `Node2` |
* `"Operator A"` is "not authorized" to invoke:
    | api          | node    |
    |--------------|---------|
    | `admin_peer` | `Node3` |
    | `admin_peer` | `Node4` |

## Clients are authorized to access APIs based on wildcard scope granted

Wildcard can be used to define access scope. `rpc://eth_*` means all APIs in `eth` namespace

* `"Client 1"` is "authorized" to invoke:
    | api               | node    |
    |-------------------|---------|
    | `eth_blockNumber` | `Node1` |
    | `eth_accounts`    | `Node2` |
* `"Client 1"` is "not authorized" to invoke:
    | api                     | node    |
    |-------------------------|---------|
    | `personal.listAccounts` | `Node1` |
    | `admin_datadir`         | `Node2` |