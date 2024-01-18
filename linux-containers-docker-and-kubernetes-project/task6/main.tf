# 1. Specify the version of the AzureRM Provider to use
terraform {
  required_providers {
    azurerm = {
      source = "hashicorp/azurerm"
      version = "=3.39.0"
    }
  }
}

# 2. Configure the AzureRM Provider
provider "azurerm" {
  features {}
}

# 3. Create an Azure Front Door resource

resource "azurerm_frontdoor" "frontdoor" {
  name                = var.frontdoor_name
  resource_group_name = var.resource_group_name

  frontend_endpoint {
    name      = "frontendEndpoint"
    host_name = "${var.frontdoor_name}.azurefd.net"
  }

  backend_pool_health_probe {
    name     = "probelogin"
    path     = "/login"
    protocol = "Http"
    probe_method = "HEAD"
    interval_in_seconds = 30
  }

  backend_pool_health_probe {
    name     = "probechat"
    path     = "/chat"
    protocol = "Http"
    probe_method = "HEAD"
    interval_in_seconds = 30
  }

  backend_pool_load_balancing {
    name                            = "wecloudloadbalancer"
    sample_size                     = 4
    successful_samples_required     = 2
    additional_latency_milliseconds = 50
  }

  backend_pool {
    name                = "wecloudbackendloginprofile"
    health_probe_name   = "probelogin"
    load_balancing_name = "wecloudloadbalancer"

    backend {
      host_header = var.gcp_ingress_external_ip
      address     = var.gcp_ingress_external_ip
      http_port   = 80
      https_port  = 443
    }

    backend {
      host_header = var.azure_ingress_external_ip
      address     = var.azure_ingress_external_ip
      http_port   = 80
      https_port  = 443
    }
  }

  backend_pool {
    name                = "wecloudbackendchat"
    health_probe_name   = "probechat"
    load_balancing_name = "wecloudloadbalancer"

    backend {
      host_header = var.gcp_ingress_external_ip
      address     = var.gcp_ingress_external_ip
      http_port   = 80
      https_port  = 443
    }
  }

routing_rule {
    name               = "loginprofilerouting"
    accepted_protocols = ["Http"]
    patterns_to_match  = ["/login", "/profile"]
    frontend_endpoints = ["frontendEndpoint"]
    
    forwarding_configuration {
      forwarding_protocol = "HttpOnly"
      backend_pool_name  = "wecloudbackendloginprofile"
    }
}

routing_rule {
    name               = "chatrouting"
    accepted_protocols = ["Http"]
    patterns_to_match  = ["/chat", "/chat/*"]
    frontend_endpoints = ["frontendEndpoint"]
    
    forwarding_configuration {
      forwarding_protocol = "HttpOnly"
      backend_pool_name  = "wecloudbackendchat"
    }
  }
}

