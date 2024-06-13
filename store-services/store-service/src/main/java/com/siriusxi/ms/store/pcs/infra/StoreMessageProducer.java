package com.siriusxi.ms.store.pcs.infra;

import com.siriusxi.ms.store.api.core.product.dto.Product;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

@Configuration
public class StoreMessageProducer {

	// Define the producer using functional style

	@Bean
	public Function<Product, Product> storeProducer() {
		return null;
	}

	@Bean
	public Function<Product, Product> recommendationsProducer() {
		return null;
	}

	@Bean
	public Function<Product, Product> reviewsProducer() {
		return null;
	}
}
