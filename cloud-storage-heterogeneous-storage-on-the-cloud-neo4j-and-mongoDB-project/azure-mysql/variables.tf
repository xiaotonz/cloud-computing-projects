// General variables
variable "location" {
    type=string
    default = "East US"
}

variable "geo_redundant_backup_enabled" {
    type=bool
    default = "false"
}

variable "administrator_login" {
    type=string
    default = "clouduser"
}

variable "administrator_password" {
    description = "Please provide Secure String Value for Admin Login Password"
    type = string
    sensitive = true
}

variable "backup_retention_days" {
    type=number
    default = 7
}

variable "previewFeature" {
    type=string
    default = ""
}

variable "resource_group_name" {
    type=string
    default = "social-network-rg"
}

variable "server_name" {
    type=string
    default = "social-network-mysql"
}

variable "sku_name" {
    type=string
    default = "GP_Gen5_2"
}

variable "skuSizeMB" {
    type=number
    default = 5120
}

variable "version_number" {
    type=string
    default = "5.7"
}

variable "skuCapacity" {
    type=number
    default = 2
}

variable "skuTier" {
    type=string
    default = "GeneralPurpose"
}

variable "skuFamily" {
    type=string
    default = "Gen5"
}