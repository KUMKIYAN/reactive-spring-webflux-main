package com.learnreactiveprogramming.service;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class FluxAndMonoGeneratorServiceTest {

    FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();

//    @BeforeEach
//    void setUp() {
//    }

    @Test
    void nameFlux() {
        var namesFlux = fluxAndMonoGeneratorService.nameFlux();
//        StepVerifier.create(namesFlux).expectNext("ramu", "pawan","kiyan").verifyComplete();
//        StepVerifier.create(namesFlux).expectNextCount(3).verifyComplete();
        StepVerifier.create(namesFlux).expectNext("ramu").expectNextCount(2).verifyComplete();
    }


    @Test
    void nameFluxMap() {
        var namesFlux = fluxAndMonoGeneratorService.nameFluxMap();
        StepVerifier.create(namesFlux).expectNext("RAMU").expectNextCount(2).verifyComplete();
    }

    @Test
    void nameFluxFilter(){
        int nameFilterLength = 4;
        var namesFlux = fluxAndMonoGeneratorService.nameFluxFilter(nameFilterLength);
        StepVerifier.create(namesFlux).expectNext("pawan","kiyan").verifyComplete();
    }

    @Test
    void nameFluxFlatMap(){
        List<String> maleEmployees = Arrays.asList("pavan", "ramu", "bhargav");
        List<String> femaleEmployees = Arrays.asList("navya", "amulya", "srikala","sarika");
        List<List<String>> allEmployees = new ArrayList<>();
        allEmployees.add(maleEmployees);
        allEmployees.add(femaleEmployees);
        var namesFlux = fluxAndMonoGeneratorService.nameFluxFlatMap(allEmployees);
        StepVerifier.create(namesFlux)
                .expectNext("pavan", "ramu", "bhargav","navya", "amulya", "srikala","sarika")
                .verifyComplete();
    }

    @Test
    void nameFluxFlatMapAsync(){
        List<String> maleEmployees = Arrays.asList("pavan", "ramu", "bhargav");
        List<String> femaleEmployees = Arrays.asList("navya", "amulya", "srikala","sarika");
        List<List<String>> allEmployees = new ArrayList<>();
        allEmployees.add(maleEmployees);
        allEmployees.add(femaleEmployees);
        var namesFlux = fluxAndMonoGeneratorService.nameFluxFlatMapAsync(allEmployees);
        StepVerifier.create(namesFlux)
                .expectNext("pavan", "ramu", "bhargav","navya", "amulya", "srikala","sarika")
                .verifyComplete();
    }

    @Test
    void nameFluxConcatMap(){
        List<String> maleEmployees = Arrays.asList("pavan", "ramu", "bhargav");
        List<String> femaleEmployees = Arrays.asList("navya", "amulya", "srikala","sarika");
        List<List<String>> allEmployees = new ArrayList<>();
        allEmployees.add(maleEmployees);
        allEmployees.add(femaleEmployees);
        var namesFlux = fluxAndMonoGeneratorService.nameFluxConcatMap(allEmployees);
        StepVerifier.create(namesFlux)
                .expectNext("pavan", "ramu", "bhargav","navya", "amulya", "srikala","sarika")
                .verifyComplete();
    }

    @Test
    void nameMonoFlatMap(){
        var namesFlux = fluxAndMonoGeneratorService.namesMono_flat_map("kiyan");
        StepVerifier.create(namesFlux)
                .expectNext(List.of("K","I","Y","A","N"))
                        .verifyComplete();
    }

    @Test
    void nameMonoFlatMapMany(){
        var namesFlux = fluxAndMonoGeneratorService.namesMono_flat_map_many("kiyan");
        StepVerifier.create(namesFlux)
                .expectNext("K","I","Y","A","N")
                .verifyComplete();
    }

    @Test
    void nameFluxTransformation(){
        List<String> maleEmployees = Arrays.asList("pavan", "ramu", "bhargav", "kck");
        var namesFlux = fluxAndMonoGeneratorService.nameFlux_transform(maleEmployees);
        StepVerifier.create(namesFlux)
                .expectNext("ramu", "kck")
                .verifyComplete();
    }

    @Test
    void nameFluxDefaultEmpty(){
        List<String> maleEmployees = Arrays.asList("pavan", "ramulo", "bhargav", "kiyan");
        var namesFlux = fluxAndMonoGeneratorService.nameFlux_transform_defaultIfEmpty(maleEmployees);
        StepVerifier.create(namesFlux)
                .expectNext("nodata")
                .verifyComplete();
    }

    @Test
    void nameFluxSwitchIfEmpty(){
        List<String> maleEmployees = Arrays.asList("pavan", "ramulo", "bhargav", "kiyan");
        var namesFlux = fluxAndMonoGeneratorService.nameFlux_transform_swtichIfEmpty(maleEmployees);
        StepVerifier.create(namesFlux)
                .expectNext("kiyan")
                .verifyComplete();
    }

    @Test
    void nameFluxConcat(){
        List<String> maleEmployees = Arrays.asList("pavan", "ramulo", "bhargav", "kiyan");
        List<String> femaleEmployees = Arrays.asList("navya", "amulya", "srikala","sarika");
        var namesFlux = fluxAndMonoGeneratorService.nameFlux_explore_concat(maleEmployees, femaleEmployees);
        StepVerifier.create(namesFlux)
                .expectNext("pavan", "ramulo", "bhargav", "kiyan", "navya", "amulya", "srikala","sarika")
                .verifyComplete();
    }

    @Test
    void nameFluxConcatWith(){
        List<String> maleEmployees = Arrays.asList("pavan", "ramulo", "bhargav", "kiyan");
        List<String> femaleEmployees = Arrays.asList("navya", "amulya", "srikala","sarika");
        var namesFlux = fluxAndMonoGeneratorService.nameFlux_explore_concatWith(maleEmployees, femaleEmployees);
        StepVerifier.create(namesFlux)
                .expectNext("pavan", "ramulo", "bhargav", "kiyan", "navya", "amulya", "srikala","sarika")
                .verifyComplete();
    }

    @Test
    void nameFluxConcatMono(){
       Mono<String> sulutation = Mono.just("Hello");
        Mono<String> person = Mono.just("kiyan");
        var namesFlux = fluxAndMonoGeneratorService.nameFlux_explore_concatMono(sulutation, person);
        StepVerifier.create(namesFlux)
                .expectNext("Hello", "kiyan")
                .verifyComplete();
    }

    @Test
    void nameFluxConcatWithMono(){
        Mono<String> sulutation = Mono.just("Hello");
        Mono<String> person = Mono.just("kiyan");
        var namesFlux = fluxAndMonoGeneratorService.nameFlux_explore_concatWithMono(sulutation, person);
        StepVerifier.create(namesFlux)
                .expectNext("Hello", "kiyan")
                .verifyComplete();
    }

    @Test
    void nameFluxMerge(){
        List<String> maleEmployees = Arrays.asList("pavan", "ramulo", "bhargav", "kiyan");
        List<String> femaleEmployees = Arrays.asList("navya", "amulya", "srikala","sarika");
        var namesFlux = fluxAndMonoGeneratorService.nameFlux_explore_merge(maleEmployees, femaleEmployees);
        StepVerifier.create(namesFlux)
                .expectNext("pavan",  "navya", "ramulo", "amulya", "bhargav",  "srikala", "kiyan", "sarika")
                .verifyComplete();
    }


    @Test
    void nameFluxMergeWith(){
        List<String> maleEmployees = Arrays.asList("pavan", "ramulo", "bhargav", "kiyan");
        List<String> femaleEmployees = Arrays.asList("navya", "amulya", "srikala","sarika");
        var namesFlux = fluxAndMonoGeneratorService.nameFlux_explore_mergeWith(maleEmployees, femaleEmployees);
        StepVerifier.create(namesFlux)
                .expectNext("pavan",  "navya", "ramulo", "amulya", "bhargav",  "srikala", "kiyan", "sarika")
                .verifyComplete();
    }


    @Test
    void nameFluxMergeWithMono(){
        String sulutation = "Hello";
        String person = "kiyan";
        var namesFlux = fluxAndMonoGeneratorService.nameFlux_explore_mergeWithMono(sulutation, person);
        StepVerifier.create(namesFlux)
                .expectNext("Hello", "kiyan")
                .verifyComplete();
    }

    @Test
    void nameFluxMergeSequentially(){
        List<String> maleEmployees = Arrays.asList("pavan", "ramulo", "bhargav", "kiyan");
        List<String> femaleEmployees = Arrays.asList("navya", "amulya", "srikala","sarika");
        var namesFlux = fluxAndMonoGeneratorService.nameFlux_explore_mergeSequentially(maleEmployees, femaleEmployees);
        StepVerifier.create(namesFlux)
                .expectNext("pavan", "ramulo", "bhargav", "kiyan", "navya", "amulya", "srikala","sarika")
                .verifyComplete();
    }

    @Test
    void nameFluxZip(){
        List<String> maleEmployees = Arrays.asList("pavan", "ramulo", "bhargav", "kiyan");
        List<String> femaleEmployees = Arrays.asList("navya", "amulya", "srikala","sarika");
        var namesFlux = fluxAndMonoGeneratorService.nameFlux_explore_zip(maleEmployees, femaleEmployees);
        StepVerifier.create(namesFlux)
                .expectNext("pavannavya", "ramuloamulya", "bhargavsrikala", "kiyansarika")
                .verifyComplete();
    }

    @Test
    void nameFluxZipWith(){
        List<String> maleEmployees = Arrays.asList("pavan", "ramulo", "bhargav", "kiyan");
        List<String> femaleEmployees = Arrays.asList("navya", "amulya", "srikala","sarika");
        var namesFlux = fluxAndMonoGeneratorService.nameFlux_explore_zipWith(maleEmployees, femaleEmployees);
        StepVerifier.create(namesFlux)
                .expectNext("pavannavya", "ramuloamulya", "bhargavsrikala", "kiyansarika")
                .verifyComplete();
    }

    @Test
    void nameFluxZipTubple(){
        List<String> frontEndEngineers = Arrays.asList("pavan", "ramulo", "bhargav", "kiyan");
        List<String> backEndEngineers = Arrays.asList("navya", "amulya", "srikala","sarika");
        List<String> dbEngineers = Arrays.asList("navya 1", "amulya 1", "srikala 1","sarika 1");
        List<String> devOpsEngineers = Arrays.asList("pavan 1", "ramulo 1", "bhargav 1", "kiyan 1");
        var namesFlux = fluxAndMonoGeneratorService
                .nameFlux_explore_zip(frontEndEngineers, backEndEngineers, dbEngineers, devOpsEngineers);
        StepVerifier.create(namesFlux)
                .expectNext("pavannavyanavya 1pavan 1", "ramuloamulyaamulya 1ramulo 1",
                        "bhargavsrikalasrikala 1bhargav 1", "kiyansarikasarika 1kiyan 1")
                .verifyComplete();
    }


    @Test
    void nameMonoZipWith(){
        Mono<String> sulutation = Mono.just("Hello");
        Mono<String> person = Mono.just("kiyan");
        var namesFlux = fluxAndMonoGeneratorService.nameMono_explore_zipWith(sulutation, person);
        StepVerifier.create(namesFlux)
                .expectNext("Hello kiyan")
                .verifyComplete();
    }

    @Test
    void nameMono() {
    }
}