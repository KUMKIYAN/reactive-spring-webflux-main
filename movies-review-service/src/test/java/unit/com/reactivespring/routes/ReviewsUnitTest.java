package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewNotFoundException;
import com.reactivespring.handler.GlobalErrorHandler;
import com.reactivespring.handler.ReviewHandler;
import com.reactivespring.repository.ReviewReactiveRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static reactor.core.publisher.Mono.when;


@WebFluxTest
@ContextConfiguration(classes = {ReviewsRouter.class, ReviewHandler.class, GlobalErrorHandler.class})
@AutoConfigureWebTestClient
public class ReviewsUnitTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ReviewReactiveRepository reviewReactiveRepository;


    @Test
    void addReview() {
        var review = new Review(null, 1L, "Awesome", 9.0);

        Mockito.when(reviewReactiveRepository.save(isA(Review.class)))
                .thenReturn(Mono.just(new Review("abc", 1L, "Awesome", 9.0)));


        webTestClient.post()
                .uri("/v1/reviews")
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Review.class)
                .consumeWith(result -> {
                    var savedReview = result.getResponseBody();
                    assert savedReview != null;
                    assert savedReview.getReviewId() != null;
                    assert savedReview.getReviewId().equals("abc");
                });
    }

    @Test
    void getAllReviews() {
        //given
        var reviewList = List.of(
                new Review(null, 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review(null, 2L, "Excellent Movie", 8.0));

        Mockito.when(reviewReactiveRepository.findAll()).thenReturn(Flux.fromIterable(reviewList));
        //when

        webTestClient
                .get()
                .uri("/v1/reviews")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .value(reviews -> {
                    assertEquals(3, reviews.size());
                });

    }


    @Test
    void updateReview() {
        var review = new Review(null, 1L, "Awesome", 9.0);

        Mockito.when(reviewReactiveRepository.save(isA(Review.class)))
                .thenReturn(Mono.just(new Review("abc", 1L, "Awesome", 9.0)));

        Mockito.when(reviewReactiveRepository.findById(isA(String.class)))
                .thenReturn(Mono.just(new Review("abc", 1L, "Awesome", 9.0)));


        webTestClient.put()
                .uri("/v1/reviews/{id}", review)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Review.class)
                .consumeWith(result -> {
                    var savedReview = result.getResponseBody();
                    assert savedReview != null;
                    assert savedReview.getReviewId() != null;
                    assert savedReview.getReviewId().equals("abc");
                    assertEquals(9.0, savedReview.getRating());
                });
    }

    @Test
    void deleteReview() {

        var reviewId = "abc";
        Mockito.when(reviewReactiveRepository.findById(isA(String.class)))
                .thenReturn(Mono.just(new Review("abc", 1L, "Awesome Movie", 9.0)));
        Mockito.when(reviewReactiveRepository.deleteById((isA(String.class))))
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/v1/reviews/{id}", reviewId)
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    void getReviewsByMovieID() {
        //given
        var reviewList = List.of(
                new Review(null, 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review(null, 1L, "Awesome Movie2", 8.0));

        Mockito.when(reviewReactiveRepository.findByMovieInfoId(isA(Long.class)))
                .thenReturn(Flux.fromIterable(reviewList));
        //when

        webTestClient.get()
                .uri("/v1/reviews?movieInfoId={id}", 1)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(3);
    }


    @Test
    void addReviewValidation() {
        var review = new Review(null, null, "Awesome", -9.0);

        webTestClient
                .post()
                .uri("/v1/reviews")
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .isEqualTo("rating.movieInfoId must not be null,rating.negative: Please pass a non-negative value");
    }

//    @Test
//    void updateReviewValidation1() {
//        var review = new Review(null, 1L, "Awesome", 9.0);
//
//        Mockito.when(reviewReactiveRepository.save(isA(Review.class)))
//                .thenReturn(Mono.just(new Review("abc", 1L, "Awesome", 9.0)));
//
//        Mockito.when(reviewReactiveRepository.findById(isA(String.class)))
//                .thenThrow(new ReviewNotFoundException("Review not found exception"));
//
//        webTestClient.put()
//                .uri("/v1/reviews/{id}", review)
//                .bodyValue(review)
//                .exchange()
//                .expectStatus()
//                .is4xxClientError()
//                .expectBody(String.class)
//                .isEqualTo("Review not found exception");
//    }

    @Test
    void updateReviewValidation2() {
        var review = new Review(null, 1L, "Awesome", 9.0);

        Mockito.when(reviewReactiveRepository.save(isA(Review.class)))
                .thenReturn(Mono.just(new Review("abc", 1L, "Awesome", 9.0)));

        Mockito.when(reviewReactiveRepository.findById(isA(String.class)))
                .thenReturn(Mono.empty());

        webTestClient.put()
                .uri("/v1/reviews/{id}", review)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody()
                .isEmpty();
    }


}
