module "helper" {
    source  = "trung/ignite/quorum//modules/docker-helper"
    version = "0.0.1-beta"

    consensus       = var.consensus
    number_of_nodes = 4
    geth = {
        container = {
            image = { name = "quorumengineering/quorum:latest", local = false }
            port  = { raft = 50400, p2p = 21000, http = 8545, ws = -1 }
        }
        host = {
            port = { http_start = 22000, ws_start = -1 }
        }
    }
    tessera = {
        container = {
            image = { name = "quorumengineering/tessera:latest", local = false }
            port  = { thirdparty = 9080, p2p = 9000 }
        }
        host = {
            port = { thirdparty_start = 9080 }
        }
    }
}

module "network" {
    source  = "trung/ignite/quorum"
    version = "0.0.1-beta"

    concensus       = module.helper.consensus
    network_name    = var.network_name
    geth_networking = module.helper.geth_networking
    tm_networking   = module.helper.tm_networking
    output_dir      = var.output_dir
}

module "docker" {
    source  = "trung/ignite/quorum//modules/docker"
    version = "0.0.1-beta"

    consensus       = module.helper.consensus
    geth            = module.helper.geth_docker_config
    tessera         = module.helper.tessera_docker_config
    geth_networking = module.helper.geth_networking
    tm_networking   = module.helper.tm_networking
    network_cidr    = module.helper.network_cidr
    ethstats_ip     = module.helper.ethstat_ip
    ethstats_secret = module.helper.ethstats_secret

    network_name       = module.network.network_name
    network_id         = module.network.network_id
    node_keys_hex      = module.network.node_keys_hex
    password_file_name = module.network.password_file_name
    geth_datadirs      = module.network.data_dirs
    tessera_datadirs   = module.network.tm_dirs
}
