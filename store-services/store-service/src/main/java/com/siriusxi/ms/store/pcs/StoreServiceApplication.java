package com.siriusxi.ms.store.pcs;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Store Composite Service", version = "1.0", description =
   " REST composite service APIs for the store-service"))
// Starting point for initiating OPENAPI
@ComponentScan("com.siriusxi.ms.store")
public class StoreServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(StoreServiceApplication.class, args);
  }
}
