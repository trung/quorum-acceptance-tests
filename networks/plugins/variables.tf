variable "consensus" {
  default = "istanbul"
}

variable "network_name" {
  default = "plugins"
}

variable "number_of_nodes" {
  default = 4
}

variable "plugin_name" {
}

variable "output_dir" {
  default = "/tmp"
}

variable "quorum_docker_image" {
  type        = object({ name = string, local = bool })
  default     = { name = "quorumengineering/quorum:latest", local = false }
  description = "Most likely used for development"
}

variable "docker_registry" {
  type = list(object({ name = string, username = string, password = string }))
  default = []
}