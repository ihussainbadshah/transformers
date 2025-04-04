[<img src="./images/logo.png" width="400" height="200"/>](./images/logo.png)

# Transformers
[![Build](https://github.com/eclipse-ecsp/transformers/actions/workflows/maven-build.yml/badge.svg)](https://github.com/eclipse-ecsp/transformers/actions/workflows/maven-build.yml)
[![License Compliance](https://github.com/eclipse-ecsp/transformers/actions/workflows/license-compliance.yml/badge.svg)](https://github.com/eclipse-ecsp/transformers/actions/workflows/license-compliance.yml)
[![Deployment](https://github.com/eclipse-ecsp/transformers/actions/workflows/maven-deploy.yml/badge.svg)](https://github.com/eclipse-ecsp/transformers/actions/workflows/maven-deploy.yml)

The `transformers` project provides serialization/deserialization support to the services. The following approaches are supported:

1. Serialization/Deserialization support for an `BlobEvent`.
2. Use of [Fast Serialization](https://github.com/RuedigerMoeller/fast-serialization/tree/master) library to implement FST based serialization/deserialization for ingestion data
3. Below Transformers along with the capability of extending `Transformer` to create a new transformer for event
   - Device Message Transformer - serialization to `byte[]` from event
   - Generic Transformer - serialization/deserialization to/from `byte[]` to generic event along with event attributes validation
   - Event data key transformer - serialization/deserialization to/from `byte[]` to key
4. Support for adding object based serializers, deserializers and subtypes to the Jackson module's `ObjectMapper`

# Table of Contents
* [Getting Started](#getting-started)
* [Usage](#usage)
* [How to contribute](#how-to-contribute)
* [Built with Dependencies](#built-with-dependencies)
* [Code of Conduct](#code-of-conduct)
* [Authors](#authors)
* [Security Contact Information](#security-contact-information)
* [Support](#support)
* [Troubleshooting](#troubleshooting)
* [License](#license)
* [Announcements](#announcements)


## Getting Started

To build the project in the local working directory after the project has been cloned/forked, run:

```mvn clean install```

from the command line interface.

### Prerequisites

1. Maven
2. Java 17

### Installation

[How to set up maven](https://maven.apache.org/install.html)

[Install Java](https://stackoverflow.com/questions/52511778/how-to-install-openjdk-11-on-windows)

### Running the tests

```mvn test```

Or run a specific test

```mvn test -Dtest="TheFirstUnitTest"```

To run a method from within a test

```mvn test -Dtest="TheSecondUnitTest#whenTestCase2_thenPrintTest2_1"```

### Deployment

`Transformers` project serves as a library for the services. It is not meant to be deployed as a service in any cloud environment.

## Usage
Add the following dependency in the target project
```
<dependency>
  <groupId>org.eclipse.ecsp</groupId>
  <artifactId>transformers</artifactId>
  <version>1.X.X</version>
</dependency>

```

### Implementing a Transformer

A custom transformer can be created for an `IgniteEvent` type by extending the `Transformer` contract.

Example:

```java
public class GenericIgniteEventTransformer implements Transformer {
    @Override
    public IgniteEvent fromBlob(byte[] value, Optional<IgniteEventBase> header) {
        // deserialization logic
    }

    @Override
    public byte[] toBlob(IgniteEvent value) {
        // serialization logic
    }
}
```

### Custom serializers, deserializers and subtypes

Services can provide custom serializers, deserializers, and subtypes to the Jackson's object mapper from the environment properties specified by the following:

```properties
#comma separated value for custom deserializer. className and it's deserializer needs to separated by :
#Example: k1:v1,k2:v2
custom.deserializers=org.eclipse.ecsp.entities.EventData:org.eclipse.ecsp.entities.EventDataDeSerializer
#Custom serializer in the form of  k1:v1,k2:v2
custom.serializers=
#Custom subtypes in the form of k1:v1,k2:v2
custom.subtypes=
```

### Implementing `IgniteEvent` attribute validation

The validation for a particular `IgniteEvent` attribute needs to be configured in the environment properties by the service.

> **_NOTE:_** attribute name needs to be followed with 'inputValidation' prefix

Example:

```properties
EventID.inputvalidation=ALPHA
BizTransactionId.inputvalidation=ALPHA_NUMERIC
Timestamp.inputvalidation=NUMERIC|13
DFFQualifier.inputvalidation=ALPHA
CorrelationId.inputvalidation=NUMERIC
MessageId.inputvalidation=NUMERIC|6
RequestId.inputvalidation=ALPHA_NUMERIC|-
SourceDeviceId.inputvalidation=ALPHA_NUMERIC
VehicleId.inputvalidation=ALPHA_NUMERIC
```

## Built With Dependencies

|                                                 Dependency                                                 | Purpose                                            |
|:----------------------------------------------------------------------------------------------------------:|:---------------------------------------------------|
|                           [Utils](https://github.com/eclipse-ecsp/utils)                                   | Logging Support                                    |
|                      [Spring Framework](https://spring.io/projects/spring-framework)                       | For writing tests                                  |
|                                     [Maven](https://maven.apache.org/)                                     | Dependency Management                              |
|                                     [Junit](https://junit.org/junit5/)                                     | Testing framework                                  |
|                                    [Mockito](https://site.mockito.org/)                                    | Test Mocking framework                             |
|                            [Power Mock](https://github.com/powermock/powermock)                            | Test Mocking framework with extra mocking features |
| [Fast Serialization](https://github.com/RuedigerMoeller/fast-serialization/tree/master?tab=readme-ov-file) | FST serialization support                          |


## How to contribute

Please read [CONTRIBUTING.md](./CONTRIBUTING.md) for details on our contribution guidelines, and the process for submitting pull requests to us.

## Code of Conduct

Please read [CODE_OF_CONDUCT.md](./CODE_OF_CONDUCT.md) for details on our code of conduct.

## Authors

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
    <tr>
	  <td align="center" valign="top" width="14.28%"><a href="https://github.com/kaushalaroraharman"><img src="https://github.com/kaushalaroraharman.png" width="100px;" alt="Kaushal Arora"/><br /><sub><b>Kaushal Arora</b></sub></a><br /><a href="https://github.com/all-contributors/all-contributors/commits?author=kaushalaroraharman" title="Code and Documentation">📖</a> <a href="https://github.com/all-contributors/all-contributors/pulls?q=is%3Apr+reviewed-by%3Akaushalaroraharman" title="Reviewed Pull Requests">👀</a></td>
    </tr>
	<tr>
	  <td align="center" valign="top" width="14.28%"><a href="https://github.com/ihussainbadshah"><img src="https://github.com/ihussainbadshah.png" width="100px;" alt="Hussain Badshah"/><br /><sub><b>Hussain Badshah</b></sub></a><br /><a href="https://github.com/all-contributors/all-contributors/commits?author=ihussainbadshah" title="Code and Documentation">📖</a> <a href="https://github.com/all-contributors/all-contributors/pulls?q=is%3Apr+reviewed-by%3Aihussainbadshah" title="Reviewed Pull Requests">👀</a></td>
    </tr>
  </tbody>
</table>

See also the list of [contributors](https://github.com/eclipse-ecsp/transformers/graphs/contributors) who participated in this project.

## Security Contact Information

Please read [SECURITY.md](./SECURITY.md) to raise any security related issues.

## Support
Please write to us at csp@harman.com


## Troubleshooting

Please read [CONTRIBUTING.md](./CONTRIBUTING.md) for details on how to raise an issue and submit a pull request to us.

## License

This project is licensed under the Apache-2.0 License - see the [LICENSE](./LICENSE) file for details.

## Announcements

All updates to this library are documented in our [Release notes](./release_notes.txt) and [releases](https://github.com/eclipse-ecsp/transformers/releases)
For the versions available, see the [tags on this repository](https://github.com/eclipse-ecsp/transformers/tags).



