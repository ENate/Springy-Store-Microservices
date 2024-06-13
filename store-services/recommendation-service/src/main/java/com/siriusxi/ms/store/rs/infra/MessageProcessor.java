package com.siriusxi.ms.store.rs.infra;

import com.siriusxi.ms.store.api.core.recommendation.RecommendationService;
import com.siriusxi.ms.store.api.core.recommendation.dto.Recommendation;
import com.siriusxi.ms.store.api.event.Event;
import com.siriusxi.ms.store.util.exceptions.EventProcessingException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.function.Consumer;

import static java.lang.String.*;

// @EnableBinding(Sink.class)
@Log4j2
@Configuration
public class MessageProcessor {

    private final RecommendationService service;

    public MessageProcessor(@Qualifier("RecommendationServiceImpl") RecommendationService service) {
        this.service = service;
    }

    // @StreamListener(target = Sink.INPUT)
    @Bean
    public Consumer<Event<Integer, Recommendation>> recommendationConsumer() {

        {
            return event -> {
                log.info("Process message created at {}...", event.getEventCreatedAt());

                switch (event.getEventType()) {
                    case CREATE -> {
                        Recommendation recommendation = event.getData();
                        log.info("Create recommendation with ID: {}/{}", recommendation.getProductId(),
                           recommendation.getRecommendationId());
                        service.createRecommendation(recommendation);
                    }
                    case DELETE -> {
                        int productId = event.getKey();
                        log.info("Delete recommendations with ProductID: {}", productId);
                        service.deleteRecommendations(productId);
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
}
