provider "google" {
  project = var.project
  region  = "us-east1"
  version = "3.25"
}

provider "google-beta" {
  project = var.project
  region  = "us-east1"
  version = "3.25"
}

terraform {
  backend "gcs" {}
}

module "k8s-workload" {
  source    = "git@gitlab.com:plos/gcp-global.git//modules/k8s-workload"
  sa_name   = "wombat-sa"
  namespace = var.namespace
  cluster   = var.cluster
}

# TODO: move this and dns record into module
resource "google_compute_global_address" "frontend" {
  name = "${var.project}-journals-frontend-ip"
}

resource "google_dns_record_set" "frontend" {
  managed_zone = "${var.project}-private-zone"
  name         = "journals.${var.namespace}.chs.plos.cloud."
  type         = "A"
  ttl          = 300
  rrdatas      = ["${google_compute_global_address.frontend.address}"]
}
