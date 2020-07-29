variable "project" {
  type        = string
  description = "name of google project (required)"
}

variable "namespace" {
  type        = string
  description = "k8s namespace (required)"
}

variable "cluster" {
  type        = string
  description = "name of k8s cluster (required)"
}
