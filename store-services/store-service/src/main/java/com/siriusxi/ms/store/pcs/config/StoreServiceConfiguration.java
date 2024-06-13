package com.siriusxi.ms.store.pcs.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class StoreServiceConfiguration {

  @Value("${api.common.version}")
  private String apiVersion;

  @Value("${api.common.title}")
  private String apiTitle;

  @Value("${api.common.description}")
  private String apiDescription;

  @Value("${api.common.termsOfServiceUrl}")
  private String apiTermsOfServiceUrl;

  @Value("${api.common.license}")
  private String apiLicense;

  @Value("${api.common.licenseUrl}")
  private String apiLicenseUrl;

  @Value("${api.common.contact.name}")
  private String apiContactName;

  @Value("${api.common.contact.url}")
  private String apiContactUrl;

  @Value("${api.common.contact.email}")
  private String apiContactEmail;

  /**
   * Will exposed on $HOST:$PORT/swagger-ui.html
   *
   * @return Docket swagger configuration
   */
  @Bean
  public GroupedOpenApi publicApi() {
    return GroupedOpenApi.builder()
       .group("REST-APIs-store-public")
       .packagesToScan("com.siriusxi.ms.store.pcs.config")
       .pathsToMatch("/**")
       .build();
  }
  /*
  Replaced configs as described in springdoc for boot 3
    The api* variables that are used to configure the Docket bean with general
    information about the API are initialized from the property file using
    Spring @Value annotations.
        */
  @Bean
  public OpenAPI storeServiceOpenAPI() {
    return new OpenAPI()
       .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
       .components(new Components().addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()))
       .info(new Info().title(apiTitle)
          .description(apiDescription)
          .version(apiVersion)
          .termsOfService(apiTermsOfServiceUrl)
          .contact(new Contact().name(apiContactName).url(apiContactUrl).email(apiContactEmail))
          .license(new License().name(apiLicense).url(apiLicenseUrl)));
  }
  private SecurityScheme createAPIKeyScheme() {
    return new SecurityScheme().type(SecurityScheme.Type.HTTP)
       .bearerFormat("JWT")
       .scheme("bearer");
  }

  @Bean
  @LoadBalanced
  public WebClient.Builder loadBalancedWebClientBuilder() {
    return WebClient.builder();
  }
}
