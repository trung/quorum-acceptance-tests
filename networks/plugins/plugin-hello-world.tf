resource "local_file" "hello-world-config" {
  count    = var.plugin_name == "hello-world" ? var.number_of_nodes : 0
  filename = format("%s/plugins/hello-world-config.json", module.network.data_dirs[count.index])
  content  = <<JSON
{
    "language": "en"
}
JSON
}
