package com.reactivespring.routes;

import com.reactivespring.handler.ReviewHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ReviewsRouter {
    @Bean
    public RouterFunction<ServerResponse> reviewRoute(ReviewHandler reviewHandler){
        return route()
                .nest(path("/v1/reviews"), builder -> {
                builder.POST("", request -> reviewHandler.addReview(request))
                        .GET("", request -> reviewHandler.getReviews(request))
                        .PUT("/{id}", request -> reviewHandler.updateReview(request))
                        .DELETE("/{id}", request -> reviewHandler.deleteReview(request))
                        .GET("/stream", request -> reviewHandler.getReviewsStream(request));
                })
                .GET("/v1/helloword", (request -> ServerResponse.ok().bodyValue("hello world")))
                .build();
    }
}
