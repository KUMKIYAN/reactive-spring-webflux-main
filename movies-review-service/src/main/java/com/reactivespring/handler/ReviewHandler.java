package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactiveRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
@Component
public class ReviewHandler {

    private ReviewReactiveRepository reviewReactiveRepository;

    public ReviewHandler(ReviewReactiveRepository reviewReactiveRepository) {
        this.reviewReactiveRepository = reviewReactiveRepository;
    }

    public Mono<ServerResponse> addReview(ServerRequest request) {
        return request.bodyToMono(Review.class)
                .flatMap(reviewReactiveRepository::save)
                .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue);
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
        return existingReview
                .flatMap(review -> request.bodyToMono(Review.class)
                .map(reqReview -> {
                    review.setComment(reqReview.getComment());
                    review.setRating(reqReview.getRating());
                    review.setMovieInfoId(reqReview.getMovieInfoId());
                    return review;
                })
                .flatMap(reviewReactiveRepository::save)
                .flatMap(ServerResponse.ok()::bodyValue));
    }

    public Mono<ServerResponse> deleteReview(ServerRequest request) {
        var reviewId = request.pathVariable("id");
        return reviewReactiveRepository.deleteById(reviewId).then(ServerResponse.noContent().build());

//        return existingReview
//                .flatMap(review -> reviewReactiveRepository.deleteById(reviewId))
//                .then(ServerResponse.noContent().build());
    }
}
