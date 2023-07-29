# Bachelor Thesis Implementations 

This repository contains all relevant implementations for my bachelor
thesis `Evaluation of gRPC APIs in Modern Microservice Architectures`.

## Repository Structure

All projects are part of a parent-pom, meaning they can all be built at once by
running `mvn clean install` in the root directory of this repository.

### examples

This project contains various examples that were also shown in the thesis:

- Examples of `.proto` files
- Examples of data serialization with `protobuf`
- Example implementation of a gRPC service and client with unary RPC, client streaming RPC, server
  streaming RPC and bidirectional streaming RPC

### proto

This folder contains all `.proto` files with the service and message definitions of the following
four implemented microservices.

### random-data-service

This folder contains the Java project for the `Random-Data-Service` that is responsible to generate
10 MB of random data. \
Specified amounts of that data can then be requests via a REST and gRPC API. This microservice is
primarily used for performance testing.

### user-service

This folder contains the Java project for the `User-Service`, which is responsible to manage user
data. \
This microservice exposes a REST and gRPC API with typical CRUD operations.

### recommendation-service

This folder contains the Java project for the `Recommendation-Service`, which is responsible to
recommend product categories based on previously purchased categories \
This microservice is used by the `Shop-Service` and exposes a REST and gRPC API to recommend product
categories.

### shop-service

This folder contains the Java project for the `Shop-Service`, which is responsible to manage
products and orders and communicates with the `User-Service` and `Recommendation-Service`. \
This microservice exposes REST and gRPC APIs with typical CRUD operations for products and orders
and additionally an endpoint to recommend products to a user based on previous purchases.

### performance-tester

This folder contains a separate application that was used to run performance tests on the previously
mentioned microservices. This performance test application executes requests to gRPC and REST API of
a microservice and measures the request duration for each request. \
Check below on how to execute the performance tests.

## Starting the Microservices

Each microservice is a Spring-Boot project that can be easily started in an IDE. Alternatively all
projects can be built to a jar by running `mvn clean install` in the root folder of this repository.
This jar is located in the `target` folder of each microservice. \
Additionally all microservices can be started by specifying a Spring-Boot profile. If no profile is
provided, then each microservice starts two web servers to expose a REST and gRPC API. To only start
a single web server with the corresponding API type, the two profiles `rest` or `grpc` can be
provided.

## Run the Performance Tester

Before the performance tester can be started, all microservices need to be built by using
the `mvn clean install` command in the root directory of this repository. \
Also make sure that no microservice is running, since the performance tester starts and stops the
required microservices for a test scenario by itself.

The performance tester is currently configured to run the same scenarios, that have been used in the
thesis.
To run the performance tester, `Java` and `Python` has to be installed, since the performance tester
is started by a python script, which is also responsible to create all the plots from the
performance test data.

To start the performance tests, navigate in a shell to the root directory of this repository (This
is important, otherwise the performance tester will fail!) \
Then execute the following command:

```
python ./performance-tester/run_performance_tests.py
```

Once all tests have completed, the raw data of the performance tests is written
to `./performance-tester/performance_test_data`. \
Plots that visualize the request durations and throughput of the gRPC and REST APIs will be written
to `./performance-tester/plots`. \
Both mentioned directories will contain a subfolder named with the timestamp of when the performance
tests were started.