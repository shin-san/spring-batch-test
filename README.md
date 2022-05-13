
# spring-batch-test
## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Requirements](#requirements)
- [Quick Start](#quick-start)
- [Testing](#testing)
- [API](#requirements)

## Introduction
A sample Spring batch service that processes an xml file and persists it to a PostgreSQL DB

## Features

Contains following relevant information
* Uses Jaxb2Marshaller in ItemStreamReader


## Requirements
The application can be run locally

### Local
* [Red Hat OpenJDK 11](https://developers.redhat.com/products/openjdk/download)
* [Maven](https://maven.apache.org/download.cgi)


### Docker
* [Docker](https://www.docker.com/get-docker)


## Quick Start
Use maven to run it locally

### Run Local
```bash
$ mvn spring-boot:run
```

Application will run by default on port `8080`

Configure the port by changing `server.port` in __application.properties__


### Run Docker
TODO: Containerise it at a later date

## Testing
TODO: Additional instructions for testing the application.


## API
The batch can be initiated through the following API:

### Request

`GET /api/batch`

    curl -v http://localhost:8080/api/batch

### Response

    HTTP/1.1 200
    Content-Length: 0
    Date: Fri, 13 May 2022 07:12:44 GMT
