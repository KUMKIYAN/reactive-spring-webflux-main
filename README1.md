## Streaming Endpoint

Streaming endpoint is a kind of endpoint which continuously send updates to the client as new data arrives

This concept is similar to Server sent events

Easy to implement spring webflux

eg: stocks and sports events updates

####  unit Testing and integration testing

Spring boot uses recent versions of Junit. So it uses Junit5.

    tasks.named('test') {
	    useJUnitPlatform()
    }

    sourceSets {
        test{
            java.srcDirs = ['src/test/java/unit', 'src/test/java/intg']
        }
    }

    Integration test is for end to end testing

## Integration test

@WebFluxTest(controllers = FluxAndMonoController.class) on the top of the class - allows to access endpoints available in the controller
@AutoConfigureWebTestClient helps us to @Autowire the WebTestClient on top of the class

## Unit Testing

Unit testing is for testing interested classes / methods by mocking the dependancy layer

### @DataMongoTest
@DataMongoTest  scan the application and look for repository classes and make that class available in your test case. We do not have to instantiate the whole spring application context in order to write an integration test for the database layer. We can test the application faster than starting from scratch.

### @ActiveProfiles(“test”)
@ActiveProfiles(“test”)  will give connection to embedded mongo db. If we give @ActiveProfiles(“local”) will take the values from the application.yml file as this file contain local profile enusre test is not override

#### Whenever we are interact with reactive repository class it will return always Flux/Mono

block() will block the previous call for asynchronous. it is require for unit or integration test.

standard way of testing anything is through unite or integration testing. Testing using postman is waste of time.

@SpringBootTest will spin up the application for testing. @ActiveProfie(“test”) will use embedded mongo db and @AutoConfigureWebTestClient give webTestClient to connect endpoints.

### Unit Testing:

Unit testing are faster compare to integration test

unit Tests are hand for bean validation

unit testing controller

	@WebFluxTest(controllers = MoviesInfoController.class)
	@AutoConfigureWebTestClient

for calling depenancy service methods that have parameter use IsA

    isA(MovieInfo.class)
	isA(String.class)

unit testing for router (functioanl web. all endpoints in one method)

    @WebFluxTest
    @ContextConfiguration(classes = {ReviewsRouter.class, ReviewHandler.class})
    @AutoConfigureWebTestClient

Here we may get error null pointer on @mockbean reviewReactiveRepository. 
We need to access when with Mockito as shown below
    
    Mockito.when(reviewReactiveRepository.save(isA(Review.class)))
                .thenReturn(Mono.just(new Review("abc", 1L, "Awesome", 9.0)));

Annotations are out of scope for functional web unlike @valid in @RequestBody

doOnNext() is a side effect function where we can raise exceptions or do validations.

We get internal server error because if we have runtime exception.

In case findByXxxId, we did not get any data from the db, then we have to give not found

    .flatMap(ServerResponse.ok()::bodyValue))
    .switchIfEmpty(ServerResponse.notFound().build());

there are two thing we keep in mind. 4xx error or 5xx error
if 4xx error means client side error. it should be handled with validator in do on next function

### Bad Request 400 

    @Autowired
    private Validator validator;

    public Mono<ServerResponse> addReview(ServerRequest request) {
        return request.bodyToMono(Review.class)
                        .doOnNext(this::validate)
                        .flatMap(reviewReactiveRepository::save)
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

### NOT FOUND - 404 

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

### In other cases 5xx. 
means server side error like internal server error or server not found

    File link
    @Component
    @Slf4j
    public class GlobalErrorHandler implements ErrorWebExceptionHandler {

        @Override
        public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
            log.error("Exception Message is {}", ex.getMessage(), ex);
            DataBufferFactory dataBufferFactory = exchange.getResponse().bufferFactory();
            var errorMessage = dataBufferFactory.wrap(ex.getMessage().getBytes());

            if(ex instanceof ReviewDataException){
                exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
                return exchange.getResponse().writeWith(Mono.just(errorMessage));
            }
            if(ex instanceof ReviewNotFoundException){
                exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
                return exchange.getResponse().writeWith(Mono.just(errorMessage));
            }
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return exchange.getResponse().writeWith(Mono.just(errorMessage));
        }
    }

While calling those services from other service we need to get not found. but we get 500 error.
so in those services we have to handle them using onStatus which accept predicate and Function. 
If predicate true then function will execute. 
        
        File link

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

            return webClient.get()
                .uri(url, movieId)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    log.info("status code is : {}", clientResponse.statusCode().value() );
                    if(clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        return Mono.error(new MoviesInfoClientException(
                                "There is no MovieInfo Available for passed in ID : " + movieId,
                                clientResponse.statusCode().value()));
                    }
                        return clientResponse.bodyToMono(String.class)
                                .flatMap(responseMessage -> Mono.error(new MoviesInfoClientException(
                                        responseMessage, clientResponse.statusCode().value()
                                )));
                })
                .bodyToMono(MovieInfo.class)
                .log();
            }
        }
        
When one server is connecting and getting response from other server, 
it is very difficult to test 4XX error and 5XX errors and socket timeout exception
Here comes wiremock into picture it acts like a server and give response we requested.
@AutoConfigureWireMock will spin up the http server and give a response to us.

   File Link


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
    }

Sinks: Live Events

Step 1: Create what type of sink you want

    Sinks.Many<MovieInfo> movieInfoSink = Sinks.many().replay().all();

Step 2: When there is a new data / event we need to link that one to sink with doOnNext

    @PostMapping("/movieinfos")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MovieInfo> addMovieInfo(@RequestBody @Valid MovieInfo movieInfo){
    return movieInfoService.addMovieInfo(movieInfo)
        .doOnNext(saveInfo -> movieInfoSink.tryEmitNext(saveInfo));
    }

Step 3: consumer endpoint will reflect that data

    @GetMapping(value = "/movieinfos/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<MovieInfo> streamMovieInfos(){
        return movieInfoSink.asFlux();
    }

This will work as streaming. Server sent event work as real time live data. Like uber car location or cricket score.









    
    