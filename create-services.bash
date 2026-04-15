#!/usr/bin/env bash

# 1. User Permissions Service
spring init \
--boot-version=3.5.4 \
--build=gradle \
--type=gradle-project \
--java-version=21 \
--packaging=jar \
--name=user-permissions-service \
--artifactId=user-permissions-service \
--package-name=org.chamsoft.smarthome.userpermissions \
--groupId=org.chamsoft.smarthome.userpermissions \
--dependencies=web \
user-permissions-service

# 2. Device Service
spring init \
--boot-version=3.5.4 \
--build=gradle \
--type=gradle-project \
--java-version=21 \
--packaging=jar \
--name=device-service \
--artifactId=device-service \
--package-name=org.chamsoft.smarthome.deviceregistry \
--groupId=org.chamsoft.smarthome.deviceregistry \
--dependencies=web \
device-service

# 3. Automation Service
spring init \
--boot-version=3.5.4 \
--build=gradle \
--type=gradle-project \
--java-version=21 \
--packaging=jar \
--name=automation-service \
--artifactId=automation-service \
--package-name=org.chamsoft.smarthome.automation \
--groupId=org.chamsoft.smarthome.automation \
--dependencies=web \
automation-service

# 4. API Gateway
spring init \
--boot-version=3.5.4 \
--build=gradle \
--type=gradle-project \
--java-version=21 \
--packaging=jar \
--name=api-gateway \
--artifactId=api-gateway \
--package-name=org.chamsoft.smarthome.gateway \
--groupId=org.chamsoft.smarthome.gateway \
--dependencies=web \
api-gateway