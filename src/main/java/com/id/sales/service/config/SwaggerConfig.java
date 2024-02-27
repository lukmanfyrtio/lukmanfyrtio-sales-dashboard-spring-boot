package com.id.sales.service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@OpenAPIDefinition(info = @Info(title = "Sales Performance Dashboard REST API", description = "The Sales Performance Dashboard REST API provides a comprehensive set of endpoints to retrieve and manage data related to sales leads, revenue, and other key performance indicators (KPIs). This API is designed to support the development of a Sales Performance Dashboard that enables users to monitor and analyze sales activities effectively.", version = "v1.0"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
public class SwaggerConfig {

}
