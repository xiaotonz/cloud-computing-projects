terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "=3.39.1"
    }
  }
}

provider "azurerm" {
  features {}
}

resource "azurerm_mysql_server" "project" {
  name                = format("%s%s","${var.server_name}","${random_string.project.result}")
  location            = var.location
  resource_group_name = var.resource_group_name
  administrator_login          = var.administrator_login
  administrator_login_password = var.administrator_password
  sku_name   = var.sku_name
  storage_mb = var.skuSizeMB
  version    = var.version_number
  backup_retention_days             = 7
  geo_redundant_backup_enabled      = false
  ssl_enforcement_enabled           = false
  ssl_minimal_tls_version_enforced  = "TLSEnforcementDisabled"
  tags = {    
    "project": "social-network"
  }
}

# This will randomly generate a unique string within the system,
# which is used for the storage account's name
resource "random_string" "project" {
  length = 8
  special = false
  upper = false
  min_lower = 1
  min_numeric = 1
}