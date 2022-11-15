package com.learnreactiveprogramming.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class FluxAndMonoGeneratorService {

    public Flux<String> nameFlux() {
        var nameFlux =  Flux.fromIterable(List.of("ramu","pawan","kiyan"));
        nameFlux.map(String::toUpperCase);
        return nameFlux;
    }

    public Flux<String> nameFluxMap() {
        return Flux.fromIterable(List.of("ramu","pawan","kiyan")).map(String::toUpperCase);
    }

    public Mono<String> nameMono() {
        return Mono.just("ramu").log();
    }



    public static void main(String[] args) {
        FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();
        fluxAndMonoGeneratorService.nameFlux()
                .subscribe( name -> System.out.println("Flux Name is " + name.toUpperCase()));
        fluxAndMonoGeneratorService.nameMono()
                .subscribe( name -> System.out.println("Mono Name is " + name.toUpperCase()));
    }

    public Flux<String> nameFluxFilter(int nameFilterLength) {
      return Flux.fromIterable(List.of("ramu","pawan","kiyan"))
              .filter(name -> name.length() > nameFilterLength).log();
    }

    public Flux<String> nameFluxFlatMap(List<List<String>> allEmployees) {
        return Flux.fromIterable(allEmployees).flatMap(Flux::fromIterable).log();
    }

    public Flux<String> nameFluxFlatMapAsync(List<List<String>> allEmployees) {
        var delay = new Random().nextInt(100);
        return Flux.fromIterable(allEmployees)
                .flatMap(Flux::fromIterable)
                .delayElements(Duration.ofMillis(delay))
               .log();

    }



    public Flux<String> nameFluxConcatMap(List<List<String>> allEmployees) {
        var delay = new Random().nextInt(100);
        return Flux.fromIterable(allEmployees)
                .concatMap(Flux::fromIterable)
                .delayElements(Duration.ofMillis(delay))
                .log();
    }


    public Mono<List<String>> namesMono_flat_map(String name){
        return Mono.just(name).map(String::toUpperCase).flatMap(this::splitStringMono).log();
    }

    private Mono<List<String>> splitStringMono(String name) {
        var charArray = name.split("");
        var charList = List.of(charArray);
        return Mono.just(charList);
    }


    public Flux<String> namesMono_flat_map_many(String name) {
        return Mono.just(name).map(String::toUpperCase).flatMapMany(this::splitString).log();
    }

    private Flux<String> splitString(String name) {
        var charArray = name.split("");
        return Flux.fromArray(charArray);
    }

    public Flux<String> nameFlux_transform(List<String> maleEmployees) {
        Function<Flux<String>, Flux<String>> filterMap = names -> names.filter(e -> e.length() <5);
        return Flux.fromIterable(maleEmployees)
                .transform(filterMap)
                .defaultIfEmpty("nodata")
                .log();
    }

    public Flux<String> nameFlux_transform_defaultIfEmpty(List<String> maleEmployees) {
        Function<Flux<String>, Flux<String>> filterMap = names -> names.filter(e -> e.length() <5);
        return Flux.fromIterable(maleEmployees)
                .transform(filterMap)
                .defaultIfEmpty("nodata")
                .log();
    }

    public Flux<String> nameFlux_transform_swtichIfEmpty(List<String> maleEmployees) {
        Function<Flux<String>, Flux<String>> filterMap = names -> names.filter(e -> e.length() <5);
        return Flux.fromIterable(maleEmployees)
                .transform(filterMap)
                .switchIfEmpty(Flux.fromIterable(List.of("kiyan")))
                .log();
    }

    public Flux<String> nameFlux_explore_concat(List<String> maleEmployees, List<String> femaleEmployee){
        var maleFlux = Flux.fromIterable(maleEmployees);
        var femaleFlux = Flux.fromIterable(femaleEmployee);
        return Flux.concat(maleFlux, femaleFlux).log();
    }

    public Flux<String> nameFlux_explore_concatWith(List<String> maleEmployees, List<String> femaleEmployee) {
        var maleFlux = Flux.fromIterable(maleEmployees);
        var femaleFlux = Flux.fromIterable(femaleEmployee);
        return maleFlux.concatWith(femaleFlux).log();
    }

    public Flux<String> nameFlux_explore_concatMono(Mono<String> sulutation, Mono<String> person) {
        return Flux.concat(sulutation, person).log();
    }

    public Flux<String> nameFlux_explore_concatWithMono(Mono<String> sulutation, Mono<String> person) {
        return sulutation.concatWith(person).log();
    }

    public Flux<String>  nameFlux_explore_merge(List<String> maleEmployees, List<String> femaleEmployees) {
        return Flux.merge(Flux.fromIterable(maleEmployees).delayElements(Duration.ofMillis(100)),
                Flux.fromIterable(femaleEmployees).delayElements(Duration.ofMillis(120))).log();
    }

    public Flux<String>  nameFlux_explore_mergeWith(List<String> maleEmployees, List<String> femaleEmployees) {
        return Flux.fromIterable(maleEmployees).delayElements(Duration.ofMillis(100)).mergeWith(
                Flux.fromIterable(femaleEmployees).delayElements(Duration.ofMillis(120))).log();
    }
    public Flux<String> nameFlux_explore_mergeWithMono(String sulutation, String person) {
        return Mono.just(sulutation).mergeWith(Mono.just(person)).log();
    }


    public Flux<String> nameFlux_explore_mergeSequentially(List<String> maleEmployees, List<String> femaleEmployees) {
        return Flux.mergeSequential(Flux.fromIterable(maleEmployees).delayElements(Duration.ofMillis(100)),
                Flux.fromIterable(femaleEmployees).delayElements(Duration.ofMillis(120))).log();

    }

    public Flux<String> nameFlux_explore_zip(List<String> maleEmployees, List<String> femaleEmployees) {
        return Flux.zip(Flux.fromIterable(maleEmployees), Flux.fromIterable(femaleEmployees), (male, female) ->
                male + female);
    }

    public Flux<String> nameFlux_explore_zipWith(List<String> maleEmployees, List<String> femaleEmployees) {
        return Flux.fromIterable(maleEmployees).zipWith(Flux.fromIterable(femaleEmployees), (male, female) ->
                male + female);
    }

    public Flux<String> nameFlux_explore_zip(List<String> e1, List<String> e2, List<String> e3, List<String> e4) {
        return Flux.zip(Flux.fromIterable(e1), Flux.fromIterable(e2), Flux.fromIterable(e3) , Flux.fromIterable(e4))
                .map(t4 -> t4.getT1() + t4.getT2() + t4.getT3() + t4.getT4());
    }

    public Mono<String> nameMono_explore_zipWith(Mono<String> sulutation, Mono<String> person) {
        return sulutation.zipWith(person).map(t2 -> t2.getT1() + " "+ t2.getT2());
    }
}
