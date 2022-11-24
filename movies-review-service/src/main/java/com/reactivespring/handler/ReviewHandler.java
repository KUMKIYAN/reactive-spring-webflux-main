package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewDataException;
import com.reactivespring.exception.ReviewNotFoundException;
import com.reactivespring.repository.ReviewReactiveRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ReviewHandler {

    @Autowired
    private Validator validator;

    private ReviewReactiveRepository reviewReactiveRepository;

    Sinks.Many<Review> reviewsSink = Sinks.many().replay().all();

    public ReviewHandler(ReviewReactiveRepository reviewReactiveRepository) {
        this.reviewReactiveRepository = reviewReactiveRepository;
    }

    public Mono<ServerResponse> addReview(ServerRequest request) {
        return request.bodyToMono(Review.class)
                .doOnNext(this::validate)
                .flatMap(reviewReactiveRepository::save)
                .doOnNext(savedReview -> reviewsSink.tryEmitNext(savedReview))
                .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue);
    }

    private void validate(Review review) {
        var constrainViolation = validator.validate(review);
        log.info("ConstrainViolation {}",constrainViolation);
        if (constrainViolation.size() > 0) {
            var errorMessage = constrainViolation
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .sorted()
                    .collect(Collectors.joining(","));
            throw new ReviewDataException(errorMessage);
        }
    }

    public Mono<ServerResponse> getReviews(ServerRequest request) {
        var movieInfoId = request.queryParam("movieInfoId");
        if(movieInfoId.isPresent()) {
            var reviewsFlux = reviewReactiveRepository.findByMovieInfoId(Long.valueOf(movieInfoId.get()));
            return buildReviewResponse(reviewsFlux);
        } else {
            var reviewsFlux = reviewReactiveRepository.findAll();
            return buildReviewResponse(reviewsFlux);

        }
    }

    private Mono<ServerResponse> buildReviewResponse(Flux<Review > reviewsFlux){
        return ServerResponse.ok().body(reviewsFlux, Review.class).log();
    }

    public Mono<ServerResponse> updateReview(ServerRequest request) {
        var reviewId = request.pathVariable("id");
        var existingReview =  reviewReactiveRepository.findById(reviewId);
               // .switchIfEmpty(Mono.error(new ReviewNotFoundException("Review not found the given review Id" + reviewId)));
        return existingReview
                .flatMap(review -> request.bodyToMono(Review.class)
                .map(reqReview -> {
                    review.setComment(reqReview.getComment());
                    review.setRating(reqReview.getRating());
                    review.setMovieInfoId(reqReview.getMovieInfoId());
                    return review;
                })
                .flatMap(reviewReactiveRepository::save)
                .flatMap(ServerResponse.ok()::bodyValue))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> deleteReview(ServerRequest request) {
        var reviewId = request.pathVariable("id");
        return reviewReactiveRepository.deleteById(reviewId).then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> getReviewsStream(ServerRequest request) {
       return ServerResponse.ok().contentType(MediaType.APPLICATION_NDJSON)
                .body(reviewsSink.asFlux(), Review.class)
                .log();
    }
}
