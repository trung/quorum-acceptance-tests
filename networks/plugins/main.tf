locals {
  standard_apis = "admin,db,eth,debug,miner,net,shh,txpool,personal,web3,quorum,${var.consensus}"
  apis          = var.plugin_name == "hello-world" ? local.standard_apis + ",plugin@helloworld" : local.standard_apis
}

resource "local_file" "plugin-settings" {
    count    = var.number_of_nodes
    filename = format("%s/plugin-settings.json", module.network.data_dirs[count.index])
    content  = <<JSON
{
	"providers": {
%{ if var.plugin_name == "hello-world" ~}
		"helloworld": {
			"name": "quorum-plugin-hello-world-go",
			"version": "1.0.0",
			"config": "${format("file://%s/plugins/hello-world-config.json", "/data/qdata")}"
		}
%{ endif ~}
%{ if var.plugin_name == "security" ~}
		"security": {
			"name": "quorum-plugin-security",
			"version": "1.0.0-beta",
			"config": "${format("file://%s/plugins/security-config.json", "/data/qdata")}"
		}
%{ endif ~}

	}
}
JSON
}