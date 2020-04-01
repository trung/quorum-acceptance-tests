locals {
  oauth2_server            = format("%s-oauth2-server", var.network_name)
  oauth2_server_serve_port = { internal = 4444, external = 4444 } # for client to connect and authenticate
  oauth2_server_admin_port = { internal = 4445, external = 4445 } # for admin
}

resource "docker_container" "hydra" {
  image    = "oryd/hydra:v1.3.2-alpine"
  name     = local.oauth2_server
  hostname = local.oauth2_server
  networks_advanced {
    name = module.docker.docker_network_name
  }
  env = [
    "URLS_SELF_ISSUER=https://goquorum.com/oauth/",
    "DSN=memory",
    "STRATEGIES_ACCESS_TOKEN=jwt"
  ]
  restart = "unless-stopped"
  ports {
    internal = local.oauth2_server_serve_port.internal
    external = local.oauth2_server_serve_port.external
  }
  ports {
    internal = local.oauth2_server_admin_port.internal
    external = local.oauth2_server_admin_port.external
  }
  healthcheck {
    test         = ["CMD", "nc", "-vz", "localhost", local.oauth2_server_serve_port.internal]
    interval     = "3s"
    retries      = 10
    timeout      = "3s"
    start_period = "5s"
  }
}

resource "local_file" "security-config" {
  count    = var.plugin_name == "security" ? var.number_of_nodes : 0
  filename = format("%s/plugins/security-config.json", module.network.data_dirs[count.index])
  content  = <<JSON
{
  "tls": {
    "auto": true,
    "certFile": "/tmp/cert.pem",
    "keyFile": "/tmp/key.pem"
  },
  "tokenValidation": {
    "issuers": [
      "https://goquorum.com/oauth/"
    ],
    "jws": {
      "endpoint": "https://${local.oauth2_server}:${local.oauth2_server_serve_port.internal}/.well-known/jwks.json",
      "tlsConnection": {
        "insecureSkipVerify": true
      }
    },
    "jwt": {
      "authorizationField": "scp",
      "preferIntrospection": false
    }
  }
}
JSON
}
