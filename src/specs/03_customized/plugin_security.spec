# Pluggable Architecture with security plugin

 Tags: networks/plugins, plugin-security, isolate

* Configure the authorization server to grant `"Client_1"` access to scopes `"rpc://eth_*,rpc://rpc_modules"` in `"Node1,Node2"`
* Configure the authorization server to grant `"Operator_A"` access to scopes `"rpc://admin_peers"` in `"Node1,Node2"`

## Clients are authorized to access APIs based on specific scope granted

* `"Client_1"` requests access token for scope(s) `"rpc://rpc_modules"` and audience(s) `"Node1,Node2"` from the authorization server
* `"Client_1"` is responded with "success" when trying to:
    | callApi       | targetNode |
    |---------------|------------|
    | `rpc_modules` | `Node1`    |
    | `rpc_modules` | `Node2`    |
* `"Client_1"` is responded with "access denied" when trying to:
    | callApi                 | targetNode |
    |-------------------------|------------|
    | `admin_peers`           | `Node1`    |
    | `personal_listAccounts` | `Node2`    |
* `"Operator_A"` requests access token for scope(s) `"rpc://admin_peers"` and audience(s) `"Node1,Node2"` from the authorization server
* `"Operator_A"` is responded with "success" when trying to:
    | callApi       | targetNode |
    |---------------|------------|
    | `admin_peers` | `Node1`    |
    | `admin_peers` | `Node2`    |
* `"Operator_A"` is responded with "access denied" when trying to:
    | callApi           | targetNode |
    |-------------------|------------|
    | `eth_blockNumber` | `Node1`    |
    | `eth_accounts`    | `Node2`    |

## Clients are authorized to access APIs based on wildcard scope granted

Wildcard can be used to define access scope. `rpc://eth_*` means all APIs in `eth` namespace

* `"Client_1"` requests access token for scope(s) `"rpc://eth_*"` and audience(s) `"Node1,Node2"` from the authorization server
* `"Client_1"` is responded with "success" when trying to:
    | callApi           | targetNode |
    |-------------------|------------|
    | `eth_blockNumber` | `Node1`    |
    | `eth_accounts`    | `Node2`    |
* `"Client_1"` is responded with "access denied" when trying to:
    | callApi                 | targetNode |
    |-------------------------|------------|
    | `personal_listAccounts` | `Node1`    |
    | `admin_datadir`         | `Node2`    |

## Clients are authorized to access APIs based on target nodes granted

* `"Client_1"` requests access token for scope(s) `"rpc://eth_*"` and audience(s) `"Node1,Node2"` from the authorization server
* `"Client_1"` is responded with "invalid audience claim (aud)" when trying to:
    | callApi           | targetNode |
    |-------------------|------------|
    | `eth_accounts`    | `Node3`    |
* `"Operator_A"` requests access token for scope(s) `"rpc://admin_peers"` and audience(s) `"Node1,Node2"` from the authorization server
* `"Operator_A"` is responded with "invalid audience claim (aud)" when trying to:
    | callApi       | targetNode |
    |---------------|------------|
    | `admin_peers` | `Node4`    |