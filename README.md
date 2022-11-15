# reactive-spring-webflux

## Spring Webflux

Spring webflux helps us with less response time (lanency), can handle more load , down time is less compare to spring boot applicaton.
Thread Pool sit in server. concurrency is Thread per request - this style of building API are called blocking api. 200 threads comes in embeded tomcat server
we can increase thread count but not great extend like 10,000 mean 10,000 cucurrent users. thread is expensive resouce. it take 1 mb of heap space. more thread makes system resouces reduced.
To overcome this summation of db + api + api or latancy java introduced Callbacks, Future and CompleteableFuture.

Callbacks - writing code for Callback is very difficutl and difficult to read , understand and maintain for a new user - Callbackhell
Future - introduced in java5
CompleteableFuture - introduced in java8

Reactive streams are foundation of Reactive programming

## Reactive Programming

    The reactive library here is reactive-core. Spring webflux uses behind the screen for us. reactor-test to test non-blocking code.

    //reactor-library
    implementation("io.projectreactor:reactor-core:3.4.0")

    //reactor-debug-agent
    implementation('io.projectreactor:reactor-tools:3.4.0')

    //testing
    testImplementation("io.projectreactor:reactor-test:3.4.0")


Flux objects can be created using list with below syntax.

	Flux.fromIterable(List.of("ramu","pawan","kiyan"));

Mono object can be created by passing single object or empty.

	Mono.just("ramu").log();

sequence of flow

	1. requestFordata() from subscirber to publisher
	2. onSubscirbe() from publisher to subscriber (it create subscription object in subscriber)
	3. request(unbounded) from subscirber to publisher
	4. onNext(ramu) from publisher to subscriber
	5. onNext(pawan) from publisher to subscriber
	6. onNext(kiyan) from publisher to subscriber
	7. onComplete()/onError from publisher to subscriber

Use StepVerifier from reactive test to test non blocking code.

	StepVerifier.create(namesFlux).expectNext("ramu", "pawan","kiyan").verifyComplete();
	
	StepVerifier.create(namesFlux).expectNextCount(3).verifyComplete();

Combination of both exepctNext and expectNextCount.

	StepVerifier.create(namesFlux).expectNext("ramu").expectNextCount(2).verifyComplete();

#### Data Transform:

map() Operator

    Flux.fromIterable(List.of("ramu","pawan","kiyan")).map(String::toUpperCase) -> this will do data transformation
	
    Reavtive Streams are immutable

    var nameFlux =  Flux.fromIterable(List.of("ramu","pawan","kiyan"));
            nameFlux.map(String::toUpperCase);
            return nameFlux;

    Here the value nameFlux will be in lowercase only even we applied uppercase. becuase reactive stream is immutable.

filter() operator

    Flux.fromIterable(List.of("ramu","pawan","kiyan"))
              .filter(name -> name.length() > nameFilterLength).log();

    filter and map we can use in combincations as we use in java 8 
        
flatMap() Operator
    
    Stream of steam into single steam
    Transform one source element to flux of 1 to N element
    Use it when transfomation returns a Reactive Type
    retururn flux<Type>

    Flux.fromArray create a flux object using array
    Flux.fromIterable create a flux object using List.

    Flux.fromIterable(allEmployees).flatMap(Flux::fromIterable)

    Used for asyncronous transformation. Here order is not important, the outcome should match. (unable to reproduce. need to work on it)

concateMap()
    
    works similar to flatmap. Here order is preserved. Flatmap is faster where concatmap is slower

    Flux.fromIterable(allEmployees).concatMap(Flux::fromIterable)

Apply FlatMap on Mono

    when transformation return Mono<T>
    use flatMap if the transformation involves making a REST API call or any kind of functionality that can be done asynchronously 		

FlatmapMany

    FlatmapMany will help to return flux when we apply on mono
    Mono.just(name).map(String::toUpperCase).flatMapMany(this::splitString).log();

transform()
	
    Used to transform from one type to another
    Accepts Function Functioanl interface ( input publishe(flux or mono) output also publisher (flux or mono))
    Function<Flux<String>, Flux<String>> filterMap = names -> names.filter(e -> e.length() <5);
    return Flux.fromIterable(maleEmployees).transform(filterMap).log();

defaultIfEmpty()

    There is no gaurantee for data source to emit data all the time. Sometime it returns empty.
    defaultEmpty will return source type of default data.

    Function<Flux<String>, Flux<String>> filterMap = names -> names.filter(e -> e.length() <5);
        return Flux.fromIterable(maleEmployees)
                .transform(filterMap)
                .defaultIfEmpty("nodata")
                .log();

switchIfEmpty() 
    
    switchIfEmpty will produce publisher if no data.

    Function<Flux<String>, Flux<String>> filterMap = names -> names.filter(e -> e.length() <5);
        return Flux.fromIterable(maleEmployees)
                .transform(filterMap)
                .switchIfEmpty(Flux.fromIterable(List.of("kiyan")))
                .log();

concat and concatWith:    

    Combine Flux and Mono : It require api may give flux and db will give mono. that time we may need to combine them
    concat and concatWith : used to combine two reactive steams in to one. these two operators work similar.
    	
    concat is static method
    concatWith is instance method Flux and Mono

    Flux.concat(maleFlux, femaleFlux).log();
    maleFlux.concatWith(femaleFlux).log();

merge() and mergeWith() are used to combine two publishers into One.

    merge operator takes in two arguments. we have to specify time delay if we want inter leave fasion
    Merge will happen at 100 , 120, 200, 220 sequence sequence.

    Flux.merge(Flux.fromIterable(maleEmployees).delayElements(Duration.ofMillis(100)),
                Flux.fromIterable(femaleEmployees).delayElements(Duration.ofMillis(120))).log();

    Flux.fromIterable(maleEmployees).delayElements(Duration.ofMillis(100)).mergeWith(
                Flux.fromIterable(femaleEmployees).delayElements(Duration.ofMillis(120))).log();         

    Mono.just(sulutation).mergeWith(Mono.just(person)).log();

    merge is not available on Mono class

mergeSequential()

    Merge will happen sequentially. 
   
    Flux.mergeSequential(Flux.fromIterable(maleEmployees).delayElements(Duration.ofMillis(100)),
                Flux.fromIterable(femaleEmployees).delayElements(Duration.ofMillis(120))).log();

zip()

    We can zip upto 8 publishers and return type is tuple
    Last parameter is combinator lambda where we can do transfermation.
    Zip is static method

    Flux.zip(Flux.fromIterable(maleEmployees), Flux.fromIterable(femaleEmployees), (male, female) ->
                male + female);

    Flux.zip(Flux.fromIterable(e1), Flux.fromIterable(e2), Flux.fromIterable(e3) , Flux.fromIterable(e4))
                .map(t4 -> t4.getT1() + t4.getT2() + t4.getT3() + t4.getT4());

zipWith()

    is instance method.

    Flux.fromIterable(maleEmployees).zipWith(Flux.fromIterable(femaleEmployees), (male, female) ->
                male + female);

#### Install Mongo DB in MAC

- Run the below command to install the **MongoDB**.

```
brew services stop mongodb
brew uninstall mongodb

brew tap mongodb/brew
brew install mongodb-community
```

-  How to restart MongoDB in your local machine.

```
brew services restart mongodb-community
```

#### Install Mongo DB in Windows

- Follow the steps in the below link to install Mongo db in Windows.

https://docs.mongodb.com/manual/tutorial/install-mongodb-on-windows/
