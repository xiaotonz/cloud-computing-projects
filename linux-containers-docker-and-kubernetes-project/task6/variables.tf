variable "frontdoor_name" {
  description = "Name for the Front Door instance"
  default     = "xiaotonzfrontdoor"
}

variable "resource_group_name" {
  description = "Resource group name in Azure"
  default     = "lab2group"
}

variable "gcp_ingress_external_ip" {
  description = "Ingress external IP in GCP"
  default     = "34.148.165.182"
}

variable "azure_ingress_external_ip" {
  description = "Ingress external IP in Azure"
  default     = "4.156.166.217"
}
