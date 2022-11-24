package com.reactivespring.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.reactivespring.domain.Movie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8443)
@TestPropertySource(
      properties =  {
            "restClient.moviesInfoUrl: http://localhost:8443/v1/movieinfos",
            "restClient.reviewsUrl: http://localhost:8443/v1/reviews"
        }
)
public class MoviesControllerIntgTest {
    @Autowired
    private WebTestClient webTestClient;

    @Test
    void RetrieveMovieById(){
        var movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movieinfos" + "/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type","application/json")
                        .withBodyFile("movieinfo.json")));

        stubFor(get(urlEqualTo("/v1/reviews?movieInfo=" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type","application/json")
                        .withBodyFile("reviews.json")
                )
        );
        webTestClient.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .isOk().expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                    Movie response = movieEntityExchangeResult.getResponseBody();
                    assert response.getReviewList().size() == 2;
                    assertEquals ("Batman Begins" , response.getMovieInfo().getName());

                });
    }

    @Test
    void RetrieveMovieById_404(){
        var movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movieinfos" + "/" + movieId))
                .willReturn(aResponse()
                        .withStatus(404)));

        stubFor(get(urlEqualTo("/v1/reviews?movieInfo=" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type","application/json")
                        .withBodyFile("reviews.json")
                )
        );
        webTestClient.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(String.class)
                .isEqualTo("There is no MovieInfo Available for given Id : abc");
    }

    @Test
    void RetrieveMovieById_5xx(){
        var movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movieinfos" + "/" + movieId))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Movie Info service not available")));

        stubFor(get(urlEqualTo("/v1/reviews?movieInfo=" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type","application/json")
                        .withBodyFile("reviews.json")
                )
        );
        webTestClient.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("Server Exception in MovieInfo Service Movie Info service not available");

        WireMock.verify(4,  getRequestedFor(urlEqualTo("/v1/movieinfos" + "/"+ movieId)));
    }

    @Test
    void RetrieveMovieById_404_noRetry(){
        var movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movieinfos" + "/" + movieId))
                .willReturn(aResponse()
                        .withStatus(404)));

        stubFor(get(urlEqualTo("/v1/reviews?movieInfo=" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type","application/json")
                        .withBodyFile("reviews.json")
                )
        );
        webTestClient.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(String.class)
                .isEqualTo("There is no MovieInfo Available for given Id : abc");

        WireMock.verify(1, getRequestedFor(urlEqualTo("/v1/movieinfos" + "/"+ movieId)));
    }

    @Test
    void retrieveMovieById_Reviews_404() {
        //given
        var movieId = "abc";

        stubFor(get(urlEqualTo("/v1/movieinfos/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")));

        stubFor(get(urlEqualTo("/v1/reviews?movieInfo=" + movieId))
        //stubFor(get(urlEqualTo("/v1/reviews"))
          //      .withQueryParam("movieInfo",   equalTo(movieId))
                .willReturn(aResponse()
                        .withStatus(404)));

        //when
        webTestClient.get()
                .uri("/v1/movies/{id}", "abc")
                .exchange()
                .expectStatus().is2xxSuccessful();
        //then
        // WireMock.verify(4, getRequestedFor(urlEqualTo("/v1/movieinfos/" + movieId)));;
    }


}
