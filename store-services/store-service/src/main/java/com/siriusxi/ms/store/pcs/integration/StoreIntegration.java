package com.siriusxi.ms.store.pcs.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siriusxi.ms.store.api.core.product.ProductService;
import com.siriusxi.ms.store.api.core.product.dto.Product;
import com.siriusxi.ms.store.api.core.recommendation.RecommendationService;
import com.siriusxi.ms.store.api.core.recommendation.dto.Recommendation;
import com.siriusxi.ms.store.api.core.review.ReviewService;
import com.siriusxi.ms.store.api.core.review.dto.Review;
import com.siriusxi.ms.store.api.event.Event;
import com.siriusxi.ms.store.util.exceptions.InvalidInputException;
import com.siriusxi.ms.store.util.exceptions.NotFoundException;
import com.siriusxi.ms.store.util.http.HttpErrorInfo;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;

import static com.siriusxi.ms.store.api.event.Event.Type.CREATE;
import static com.siriusxi.ms.store.api.event.Event.Type.DELETE;
import static java.lang.String.valueOf;
import static reactor.core.publisher.Flux.empty;

@Component
@Log4j2
public class StoreIntegration implements ProductService, RecommendationService, ReviewService {

    private final String PRODUCT_ID_QUERY_PARAM = "?productId=";
    // Define string based on name of bean producer
    private static final String SUPPLIER_BINDING_NAME = "storeProducer-out-0";
    private static final String REVIEW_BINDING_NAME = "reviewsProducer-out-0";
    private static final String RECOMMENDATION_BINDING_NAME = "recommendationsProducer-out-0";
    private final StreamBridge streamBridge; // Used to read from source to destination via functional style
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper mapper;
    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;
    private final int productServiceTimeoutSec;
    private WebClient webClient;

    public StoreIntegration(
            WebClient.Builder webClientBuilder,
            ObjectMapper mapper,
            // MessageSources messageSources,
            StreamBridge streamBridge,
            @Value("${app.product-service.host}") String productServiceHost,
            @Value("${app.recommendation-service.host}") String recommendationServiceHost,
            @Value("${app.review-service.host}") String reviewServiceHost,
            @Value("${app.product-service.timeoutSec}") int productServiceTimeoutSec) {

        this.webClientBuilder = webClientBuilder;
        this.mapper = mapper;
        // this.messageSources = messageSources;
        this.streamBridge = streamBridge;
        this.productServiceTimeoutSec = productServiceTimeoutSec;

        var http = "http://";

        productServiceUrl = http.concat(productServiceHost);
        recommendationServiceUrl = http.concat(recommendationServiceHost);
        reviewServiceUrl = http.concat(reviewServiceHost);
    }

    @Override
    public Product createProduct(Product body) {
        log.debug("Publishing a create event for a new product {}", body.toString());
        Event<Integer, Product> event = new Event<>(CREATE, body.getProductId(), body);
        // Sends created Product to the supplier binding name as topic
        boolean sent = streamBridge.send(SUPPLIER_BINDING_NAME, event); // Send event returns boolean
        return body;
    }

    @Retry(name = "product")
    @CircuitBreaker(name = "product")
    @Override
    public Mono<Product> getProduct(int productId, int delay, int faultPercent) {

        var url = UriComponentsBuilder
                .fromUriString(productServiceUrl
                        .concat("/products/")
                        .concat("{productId}?delay={delay}&faultPercent={faultPercent}"))
                .build(productId, delay, faultPercent);

        log.debug("Will call the getProduct API on URL: {}", url);

        return getWebClient()
                .get().uri(url)
                .retrieve().bodyToMono(Product.class)
                .onErrorMap(WebClientResponseException.class, this::handleException)
                .timeout(Duration.ofSeconds(productServiceTimeoutSec));
    }

    @Override
    public void deleteProduct(int productId) {
        log.debug("Publishing a delete event for product id {}", productId);
        /*
         * messageSources
         * .outputProducts()
         * .send(withPayload(new Event<>(DELETE, productId, null)).build());
         */
        Event<Integer, Product> event = new Event<>(CREATE, productId, null);
        boolean deletedProduct = streamBridge.send(REVIEW_BINDING_NAME, event); // Returns boolean
        log.info("Review for Product with Id {} deleted", productId);

    }

    @Override
    public Recommendation createRecommendation(Recommendation body) {
        log.debug("Publishing a create event for a new recommendation {}", body.toString());
        Event<Integer, Recommendation> event = new Event<>(CREATE, body.getRecommendationId(), body);
        boolean sent = streamBridge.send(RECOMMENDATION_BINDING_NAME, event); // Send event returns boolean
        return body;
    }

    @Override
    public Flux<Recommendation> getRecommendations(int productId) {

        var url = recommendationServiceUrl
                .concat("/recommendations")
                .concat(PRODUCT_ID_QUERY_PARAM)
                .concat(valueOf(productId));

        log.debug("Will call the getRecommendations API on URL: {}", url);

        /*
         * Return an empty result if something goes wrong to make it possible
         * for the composite service to return partial responses
         */
        return getWebClient()
                .get()
                .uri(url)
                .retrieve()
                .bodyToFlux(Recommendation.class)
                .log()
                .onErrorResume(error -> empty());
    }

    @Override
    public void deleteRecommendations(int productId) {
        Event<Integer, Recommendation> recommendationEvent = new Event<>(DELETE, productId, null);
        log.info("Deleted recommendations for product with id: {}, at: {}", productId, recommendationEvent.getEventCreatedAt());
    }

    @Override
    public Review createReview(Review body) {
        Event<Integer, Review> reviewEvent = new Event<>(DELETE, body.getReviewId(), body);
        boolean confirmSent = streamBridge.send(REVIEW_BINDING_NAME, reviewEvent);
        return body;
    }

    @Override
    public Flux<Review> getReviews(int productId) {

        var url = reviewServiceUrl
                .concat("/reviews")
                .concat(PRODUCT_ID_QUERY_PARAM)
                .concat(valueOf(productId));

        log.debug("Will call the getReviews API on URL: {}", url);

        /*
         * Return an empty result if something goes wrong to make it possible
         * for the composite service to return partial responses
         */
        return getWebClient()
                .get()
                .uri(url)
                .retrieve()
                .bodyToFlux(Review.class).log()
                .onErrorResume(error -> empty());

    }

    @Override
    public void deleteReviews(int productId) {
        Event<Integer, Review> reviewEvent = new Event<>(DELETE, productId, null);
        boolean deletedReview = streamBridge.send(REVIEW_BINDING_NAME, reviewEvent);
        log.info("Deleted reviews for product with with id: {}", productId);
    }

    private WebClient getWebClient() {
        if (webClient == null) {
            webClient = webClientBuilder.build();
        }
        return webClient;
    }

    private Throwable handleException(Throwable ex) {
        if (!(ex instanceof WebClientResponseException wcre)) {
            log.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
            return ex;
        }

        // return switch (wcre.getStatusCode()) {
        return switch (HttpStatus.valueOf(wcre.getStatusCode().value())) {
            case NOT_FOUND -> new NotFoundException(getErrorMessage(wcre));
            case UNPROCESSABLE_ENTITY -> new InvalidInputException(getErrorMessage(wcre));
            default -> {
                log.warn("Got a unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
                log.warn("Error body: {}", wcre.getResponseBodyAsString());
                throw wcre;
            }
        };
    }

    private String getErrorMessage(WebClientResponseException ex) {
        try {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>:"
                    + mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).message());
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).message();
        } catch (IOException ioException) {
            return ex.getMessage();
        }
    }
}
