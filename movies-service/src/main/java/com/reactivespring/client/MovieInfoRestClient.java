package com.reactivespring.client;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.exception.MoviesInfoClientException;
import com.reactivespring.exception.MoviesInfoServerException;
import com.reactivespring.exception.ReviewsServerException;
import com.reactivespring.util.RetryUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
@Slf4j
public class MovieInfoRestClient {

    private WebClient webClient;

    @Value("${restClient.moviesInfoUrl}")
    private String movieInfoUrl;

    public MovieInfoRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<MovieInfo> retrieveMovieInfo(String movieId){

        var url = movieInfoUrl.concat("/{id}");

        var retrySpec = Retry.backoff(3, Duration.ofMillis(500))
                // only for server exceptions not for client exception retry
                .filter(ex -> ex instanceof MoviesInfoServerException)
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                    return Exceptions.propagate(retrySignal.failure());
                });

        return webClient.get()
                .uri(url, movieId)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    log.info("status code is : {}", clientResponse.statusCode().value() );
                    if(clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        return Mono.error(new MoviesInfoClientException(
                                "There is no MovieInfo Available for given Id : " + movieId,
                                clientResponse.statusCode().value()));
                    }
                        return clientResponse.bodyToMono(String.class)
                                .flatMap(responseMessage -> Mono.error(new MoviesInfoClientException(
                                        responseMessage, clientResponse.statusCode().value()
                                )));
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    log.info("status code is : {}", clientResponse.statusCode().value() );
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMessage -> Mono.error(new ReviewsServerException(
                                    "Server Exception in MovieInfo Service " + responseMessage
                            )));

                })
                .bodyToMono(MovieInfo.class)
               // .retry(4)
               // .retryWhen(Retry.backoff(3, Duration.ofMillis(500)))
                .retryWhen(retrySpec)
                .log();
    }

    public Flux<MovieInfo> retrieveMovieInfoStream() {
        var url = movieInfoUrl.concat("/stream");


        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, (clientResponse -> {
                    log.info("Status code : {}", clientResponse.statusCode().value());
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(response -> Mono.error(new MoviesInfoClientException(response, clientResponse.statusCode().value())));
                }))
                .onStatus(HttpStatus::is5xxServerError, (clientResponse -> {
                    log.info("Status code : {}", clientResponse.statusCode().value());
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(response -> Mono.error(new MoviesInfoServerException(response)));
                }))
                .bodyToFlux(MovieInfo.class)
                //.retry(3)
                .retryWhen(RetryUtil.retrySpec())
                .log();

    }

    public Mono<MovieInfo> retrieveMovieInfo_exchange(String movieId) {

        var url = movieInfoUrl.concat("/{id}");

        return webClient.get()
                .uri(url, movieId)
                .exchangeToMono(clientResponse -> {

                    switch (clientResponse.statusCode()) {
                        case OK:
                            return clientResponse.bodyToMono(MovieInfo.class);
                        case NOT_FOUND:
                            return Mono.error(new MoviesInfoClientException("There is no MovieInfo available for the passed in Id : " + movieId, clientResponse.statusCode().value()));
                        default:
                            return clientResponse.bodyToMono(String.class)
                                    .flatMap(response -> Mono.error(new MoviesInfoServerException(response)));
                    }
                })
                .retryWhen(RetryUtil.retrySpec())
                .log();

    }

    }

