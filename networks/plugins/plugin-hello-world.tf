resource "local_file" "hello-world-config" {
  count    = var.number_of_nodes
  filename = format("%s/plugins/hello-world-config.json", module.network.data_dirs[count.index])
  content  = <<JSON
{
    "language": "en"
}
JSON
}

resource "local_file" "plugin-settings" {
  count    = var.number_of_nodes
  filename = format("%s/plugin-settings.json", module.network.data_dirs[count.index])
  content  = <<JSON
{
	"providers": {
		"helloworld": {
			"name": "quorum-plugin-hello-world-go",
			"version": "1.0.0",
			"config": "${format("file://%s/plugins/hello-world-config.json", "/data/qdata")}"
		}
	}
}
JSON
}
