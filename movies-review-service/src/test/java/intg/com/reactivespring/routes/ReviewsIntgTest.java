package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
public class ReviewsIntgTest {
    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ReactiveMongoRepository reactiveMongoRepository;

    static String REVIEWS_URL = "/v1/reviews";

    @BeforeEach
    void setup(){
        var reviewList = List.of(
                new Review(null, 1L, "super", 8.0),
                new Review(null, 1L, "awesome", 10.0),
                new Review("xyz", 2L, "excellent", 9.0));
        reactiveMongoRepository.saveAll(reviewList).blockLast();
    }

    @AfterEach
    void tearDown(){
        reactiveMongoRepository.deleteAll().block();
    }

    @Test
    void addReview() {
        var review = new Review("xyz", 1L, "super", 8.0);

        webTestClient.post()
                .uri(REVIEWS_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Review.class).consumeWith(mo -> {
                    var savedReview = mo.getResponseBody();
                    assert savedReview != null;
                    assert savedReview.getReviewId() != null;
                });
    }

        @Test
        void updateReview(){
           var reviewId = "xyz";
            var review = new Review("xyz", 1L, "super", 8.0);

            webTestClient.put()
                    .uri(REVIEWS_URL+"/{id}", reviewId)
                    .bodyValue(review)
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody(Review.class).consumeWith(updatedReview -> {
                        var latestReview = updatedReview.getResponseBody();
                        assert latestReview != null;
                        assert latestReview.getReviewId() !=null;
                        assert latestReview.getReviewId().equals("xyz");
                        assert latestReview.getRating() == 8.0 ;
                        assert latestReview.getComment().equals("super");
                        assert latestReview.getMovieInfoId() == 1L;
                    });

    }

    @Test
    void deleteReview(){
        var reviewId = "xyz";
        webTestClient.get()
                .uri(REVIEWS_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(3);

        webTestClient.delete()
                .uri(REVIEWS_URL+"/{id}", reviewId)
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        webTestClient.get()
                .uri(REVIEWS_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(2);

    }

    @Test
    void getReviews(){
        webTestClient.get()
                .uri(REVIEWS_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(3);
    }

    @Test
    void getReviewsByMovieId(){
        webTestClient.get()
                .uri(REVIEWS_URL+"?movieInfoId={id}", 1)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(2);
    }
}
