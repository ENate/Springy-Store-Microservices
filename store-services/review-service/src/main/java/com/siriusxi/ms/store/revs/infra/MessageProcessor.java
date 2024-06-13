package com.siriusxi.ms.store.revs.infra;

import com.siriusxi.ms.store.api.core.review.ReviewService;
import com.siriusxi.ms.store.api.core.review.dto.Review;
import com.siriusxi.ms.store.api.event.Event;
import com.siriusxi.ms.store.util.exceptions.EventProcessingException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

import static java.lang.String.valueOf;

// @EnableBinding(Sink.class) deprecated since cloud 2020
@Log4j2
@Configuration
public class MessageProcessor {

    private final ReviewService service;

    public MessageProcessor(@Qualifier("ReviewServiceImpl") ReviewService service) {
        this.service = service;
    }

    // @StreamListener(target = Sink.INPUT) - deprecated
    // Use of functional style
    @Bean
    public Consumer<Event<Integer, Review>> reviewConsumer() {
        return event -> {

            log.info("Process message created at {}...", event.getEventCreatedAt());

            switch (event.getEventType()) {
                case CREATE -> {
                    Review review = event.getData();
                    log.info("Create review with ID: {}/{}", review.getProductId(),
                       review.getReviewId());
                    service.createReview(review);
                }
                case DELETE -> {
                    int productId = event.getKey();
                    log.info("Delete review with Product Id: {}", productId);
                    service.deleteReviews(productId);
                }
                default -> {
                    String errorMessage =
                       "Incorrect event type: "
                          .concat(valueOf(event.getEventType()))
                          .concat(", expected a CREATE or DELETE event");
                    log.warn(errorMessage);
                    throw new EventProcessingException(errorMessage);
                }
            }

            log.info("Message processing done!");
        };
    }
}
