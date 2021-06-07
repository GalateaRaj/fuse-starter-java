# Fuse Documentation

- [Lambdas and when to use them](#Lambdas-and-when-to-use-them)
- [Lombok](#Lombok)
    + [Annotations for mutable entities](#Annotations-for-mutable-entities)
    + [Annotations for immutable entities](#Annotations-for-immutable-entities)
    + [Annotations for components and services](#Annotations-for-components-and-services)
    + [Note on the use of @Builder](#note-on-the-use-of-builder)
- [Spring REST services](#Spring-REST-services)
    + [Tracing messages](#Tracing-messages)
    + [Exception handling](#Exception-handling)
    + [Swagger Documentation and UI](#Swagger-Documentation-and-UI)
    + [Content Negotiation](#Content-Negotiation)
- [FeignClient interaction with HTTP endpoints](#FeignClient-interaction-with-HTTP-endpoints)
- [JMS (Java Messaging Service)](#jms-java-messaging-service)
    + [JMS Queue vs. JMS Topic](#jms-queue-vs-jms-topic)
    + [JMS Listener](#JMS-Listener)
    + [Listener Containers and Listener Container Factories](#Listener-Containers-and-Listener-Container-Factories)
    + [Message Handling and Jackson Conversion](#Message-Handling-and-Jackson-Conversion)
    + [Important Fuse JMS Files](#Important-Fuse-JMS-Files)
- [Logging](#Logging)
    + [File vs. stdout/stderr](#file-vs-stdoutstderr)
    + [Single line exception logging](#Single-line-exception-logging)
- [Wit.ai](#witai)
    + [Entities](#Entities)
    + [Handling Data](#Handling-Data)
- [Custom Exceptions: when and how](#custom-exceptions-when-and-how)
- [Galatea coding standards](#Galatea-coding-standards)
    + [Based on Google Style for Java](#Based-on-Google-Style-for-Java)
    + [Extra rules](#Extra-rules)
    + [Sonar Quality Profile](#Sonar-Quality-Profile)
- [Protobuf](#Protobuf)
    + [Message Format](#Message-Format)
    + [JMS Listener](#JMS-Listener)
    + [HTTP Endpoint](#HTTP-Endpoint)
- [CI builds](#CI-builds)
    + [Jenkins](#Jenkins)
        * [Pipeline stages](#Pipeline-stages)
            - [Build](#Build)
            - [Unit Tests](#Unit-Tests)
            - [SonarQube](#SonarQube)
            - [Quality gate](#Quality-gate)
            - [Deploy](#Deploy)
            - [Integration tests](#Integration-tests)
            - [Performance tests](#Performance-tests)
            - [Shutdown](#Shutdown)
    + [SonarQube vs. Checkstyle](#sonarqube-vs-checkstyle)
    + [JaCoCo](#JaCoCo)
    + [Domain Objects](#Domain-Objects)
    + [Interacting with a Database](#Interacting-with-a-Database)
- [Cloud Foundry](#Cloud-Foundry)
    + [Overview](#Overview)
    + [Web UI](#Web-UI)
    + [CF CLI](#CF-CLI)
    + [Usage](#Usage)

## Lambdas and when to use them
## Lombok
[Project Lombok](https://projectlombok.org/) auto-generates a variety of boilerplate code artifacts such as field getters and setters, constructors, hashcode, equals, tostring() and others. It also auto-generates the Builder Pattern, making it a no-brainer to use in your Java application.

For a brief overview of the various annotations and what they do, check their [documentation](https://projectlombok.org/features/all).

### Annotations for mutable entities
`@Data` - adds getters and setters for all fields, toString(), hashCode() and equals(). It will also add a constructor with parameters for any @NonNull or final fields.
`@Builder` - implements the builder pattern.  
`@AllArgsConstructor(access = AccessLevel.PRIVATE)` - explicitly makes the all args constructor private, forcing client code to use the builder class instead.

### Annotations for immutable entities
`@Value` - marks all fields as private and final and generates only getters for all fields. Also marks the class itself final so it cannot be inherited.  
`@Builder` - implements the builder pattern.  
`@AllArgsConstructor(access = AccessLevel.PRIVATE)` - explicitly makes the all args constructor private, forcing client code to use the builder class instead.

### Annotations for components and services
`@RequiredArgsConstructor` + `@NonNull` on fields that should be injected - allows Spring to Autowire dependencies into the component via constructor injection, which is preferred over field injection  
`@RequiredArgsConstructor(onConstructor = @__({@Autowired}))` - Spring 4.3+ implicitly adds @Autowired on the constructor if there is only one, but for Spring 4.2 and below this can be used instead to do constructor injection with Lombok  
`@Slf4j` - adds a logger

### Note on the use of @Builder
The `SettlementMission` and `TradeAgreement` domain objects use Lombok `@Builder` annotations to automatically create builders for them. Both `@AllArgsConstructor` and `@NoArgsConstructor` are included on these classes, even though `@Builder` should automatically generate at least an All Args Constructor for the class. When you remove both `@AllArgsConstructor` and `@NoArgsConstructor` from an `@Builder` class, the jackson json parser fails (it needs a default no args constructor, which `@Builder` does not provide). But, when you only remove `@AllArgsConstructor` (which `@Builder` should provide automatically), `@Builder` throws an error saying that the all args constructor is undefined. The best solution is to include both constructor annotations. The only alternative is to explicitly define the constructors within the class. For now, both Lombok annotations are used as this reduces boilerplate code.


## Spring REST services
### Tracing messages
Every "request" to an entry point should:
1. Be logged
2. Set the thread name to some id that identifies the request
3. Set the mdc (Mapped Diagnostic Context) with any other context info
4. Be timed
5. If an error is thrown, that error should be logged as well

The Fuse project uses a combination of Log4J, Aspect4Log and the Tracer.java class to accomplish this. We’ll follow a Post request to the SettlementRestController class starting in the doFilterInternalHelper() method in the FuseWebRequestTraceFilter.java file.

The AutoClosedTrace object created kicks off our initial logging of the request. startTrace() creates an initial time stamp for the request and logs it. createInternalRequestId() creates a new ID for the request that will track the request through its processing and logs this ID. This function also sets the ID in the MDC. 

Back in doFilterInternalHelper(), the request is passed to the SettlementRestController class via the t.runAndTraceSuccess() method. Upon passing the request to the settleAgreement() method, Aspect4Log logs the ID created in the Tracer class as well as all the information contained in the Post request. From here, the Post message is processed by the SettlementRestController and the SettlementService classes.

Once the processing has returned to doFilterInternalHelper(), processing of the request has been completed. What is left to do is set a final timestamp and save the trace information to the database.

### Exception handling
There are several solutions to handling exceptions with Spring. Option 3 - @ControllerAdvice is the preferred method.
1. At the Controller level with @ExceptionHandler
    * Major drawback - the @ExceptionHandler annotated method is only active for that particular Controller, not globally for the entire application
2. Using a HandlerExceptionResolver (resolves any exception thrown by the application)
    * Limitation - no control over the body of the response
3. Using @ControllerAdvice
    * Consolidates @ExceptionHandler annotations into a single, global error handling component
    * Allows full control over the body of the response as well as the status code
    * Allows mapping of several exceptions to the same method, to be handled together
    * Note - the exception declared with @ExceptionHandler must match the exception used as the argument to the method

How to use @ControllerAdvice
1. Create a class that will be used to handle all exceptions thrown by Controllers and annotate it with @ControllerAdvice. In the Fuse project, the RestExceptionHandler class is the class annotated with @ControllerAdvice.
2. Create a separate controller class and annotate it with either @Controller or @RestController. In the Fuse project, the SettlementRestController class is annotated with the @RestController class.
3. In the @ControllerAdvice class, add functions to handle exceptions that can be thrown by the controller classes with the signature:  
    @ExceptionHandler(exceptionName.class)  
    protected/public ResponseEntity\<Object> funcName( final exceptionName exc)  
4. Now, when an exception is thrown in a @Controller class, it will be handled by the function defined in the @ControllerAdvice class.

### Swagger Documentation and UI  
<https://springfox.github.io/springfox/docs/current/>  
<https://www.baeldung.com/swagger-2-documentation-for-spring-rest-api>  
Start up the application and navigate to <http://localhost:8080/swagger-ui.html>

### Content Negotiation
Spring supports content negotiation between the client and the server. This allows the client to send in request bodies with various formats (e.g. JSON, XML, etc.) that can be automatically converted to Java objects, and the server to send response bodies in various formats that can be automatically converted from Java objects. The incoming format is specified by the request’s Content-Type header, and the format(s) the client wants in the response is specified by the request’s Accept header.

An example of configuring Spring to support this content negotiation and configuring what converters can be used to convert between HTTP bodies and Java objects can be seen in the Fuse MvcConfig class, specifically the `configureContentNegotiation()` and `configureMessageConverters()` methods overridden from `WebMvcConfigurerAdapter`. The Fuse example uses both predefined converters and custom converters created by implementing `HttpMessageConverter` (via `AbstractHttpMessageConverter`, which adds some convenience).

Specifying which content types are supported by a particular endpoint are done by setting the `consumes` (for incoming content types) and `produces` (for outgoing types) values on the `@RequestMapping` annotation of an endpoint. See `SettlementRestController` for examples.

<https://www.baeldung.com/spring-mvc-content-negotiation-json-xml>


## FeignClient interaction with HTTP endpoints
Feign is a tool that provides a simple way for constructing an interface with HTTP endpoints we want to connect to. Spring provides us with a way to create a Feign instance in the form of the @FeignClient annotation. To create a Feign client, we create an interface that represents the client and add the annotation `@FeignClient`. We then define methods for various REST commands in our interface and annotate them with Spring’s `@RequestMapping` annotation.  

`@RequestMapping` allows us to define a java method that, when called, makes an HTTP request. This annotation supports multiple parameters for customizing our request. For example, the ‘method’ parameter allows us to specify the type of HTTP request we want to send. The following is an example usage of `@FeignClient` from Fuse.
```java
@FeignClient(name="QuoteGetter", url = "${quote-getter.url}")
public interface QuoteGetter {
  @RequestMapping(method = RequestMethod.GET,headers = "X-Mashape-Key=${quote-getter.token}")
  Quote[] getQuote();
}
```

The above code generates a FeignClient capable of getting a response from a webservice that provides famous quotes. In the `@FeignClient` annotation, we define an arbitrary name and a URL for our client to point to. In the  `@RequestMapping` annotation, we specify that when we call getQuote(), we want to execute an HTTP GET. We also provide the authentication key using the ‘headers’ parameter.  

Not only does Feign provide us with an easy way to interact with REST endpoints, it also helps us map their responses to our custom Java objects. In the above example, getQuote() returns type Quote[]. Quote is an object that contains a quote and the quote’s source. When we call the webservice, it returns a JSON with the following format.

```json
[
    {
        "quote": "Greed, for lack of a better word, is good.",
        "author": "Wall Street",
        "category": "Movies"
    }
]
```

What we have here is a JSON representing an array (because of the outer square braces) containing a single object that has fields, “quote”, “author”, and “category”. Wit helps to automatically identify this pattern and populate a custom data structure in accordance with the JSON, in this case it is Quote[] (which will contain a single Quote). This object population is actually handled by JacksonConverter. To see how we set this as our converter, look at RestClientConfig.java


## JMS (Java Messaging Service)
Java Messaging Service (JMS) is an asynchronous communication tool that can be used to send and receive messages. In Fuse, we are only worried about the receiving of messages. In order to do this, we must connect to a JMS queue and listen for incoming messages. The way to accomplish this in Spring Boot is through the use of listener container and factory beans.

### JMS Queue vs. JMS Topic
When thinking about JMS, we want to think about how we’re sending out messages, and who will receive them. JMS provides __queues__, which implement a one-to-one messaging system where one Publisher (or Producer) publishes a message that is picked up by one Subscriber (or Consumer). Even if a consumer isn’t there to consume the message, it will be kept in the queue until it is picked up. __Topics__, on the other hand, establish a dynamic where a Publisher publishes a message, and any party who is interested in receiving that message can subscribe to that topic. In other words, a Publisher’s messages go to zero to many subscribers, and a party must have an active subscription at the time of publishing to receive a copy of the message. Decide which system works best for what you’re looking to implement.

### JMS Listener
The most basic component of the JMS listening system is the listener itself. The listener is the method that receives message payload data and performs some application specific operations on that data. In Fuse, the listener is specified in __SettlementListener.java__ as the method settleAgreement. This method receives trade agreement information from the JMS message and uses it to spawn settlement missions.  
The JMS annotation, __@JmsListener__, is what notifies Spring that this is the method we will use as our listener. We supply it two elements, destination and concurrency. 
* __Destination__ is the location of the JMS queue we wish to listen on. (This is the only element required by Spring)
* __Concurrency__ is the number of concurrent connections to the queue we will allow at one time.
These values are specified within __resources/application.yml__

### Listener Containers and Listener Container Factories
When using Spring Boot to interact with a JMS queue, we must use a specific approach that Spring Boot helps us achieve. That is using Listener Containers and Listener Container Factories.  
The __listener containers__ are what calls the listener using the __invokeListener__ method. In fuse we override this method and add some message tracing functionality.  
The __listener container factory__ is in charge of producing instances of listener containers using the __createContainerInstance__ method. We override this method so that it returns our custom FuseMessageListenerContainer instead of the default. Here Spring Boot is implementing an example of the __factory pattern__.

### Message Handling and Jackson Conversion
Incoming JMS message data must be converted to a usable format. In our case, the message payload will contain __TradeAgreements__. Before our __SettlementJmsListener__ can spawn SettlementMissions from an incoming message, the message payload must be converted to TradeAgreement objects. So, before a message is given to our JmsListener, settleAgreement, it will be processed by a message converter.  
To do this, we use the __Jackson2MessageConverter__. Spring Boot uses a MessageHandlerMethodFactory, a factory for providing message converters to incoming messages. In Fuse, we configure this factory (jmsGHandlerMethodFactory) to use an instance of a Jackson converter we’ve named __jacksonJmsMessageConverter__. This configuration happens in JmsConfig.java.

### Important Fuse JMS Files
__org.galatea.starter.entrypoint.SettlementJmsListener__ - shows how you listen for messages.  
__org.galatea.starter.entrypoint.SettlementJmsTopicPublisher__ - demonstrates how to publish messages to a topic using JmsTemplate  
__org.galatea.starter.utils.jms.FuseJmsListenerContainerFactory__ - provides a custom "listener container" factory (which is a spring jms concept). We use our own factory, so we can create our own "listener container".  
__org.galatea.starter.utils.jms.FuseMessageListenerContainer__ - is a custom listener container. This is the code that will actually call the JMS listener that you have registered. You'll notice that we populate our trace repository here. This allows us to capture every message we process and the resulting outcome.  
__org.galatea.starter.JmsConfig__ - is the spring java config related to jms  
__org.galatea.starter.entrypoint.SettlementJmsListenerTest__ - shows you how to test a jms listener. SpringBoot fires up an embedded ActiveMQ broker for the test. It's important to look at the mentioned annotated with @After in ASpringTest. You'll see that we tear down the jms connection after each test to ensure isolation between tests. This is important.  
__org.galatea.starter.entrypoint.SettlementJmsTopicPublisherTest__ - tests the functionality of the JMS Topic Publisher.  


## Logging
### File vs. stdout/stderr
Whether logging to a file or to stdout/stderr, we use Log4j2.  

It is better to print logging output to a file when working with applications that are running on the client side. An example of a log configuration file that accomplishes this is shown in log4j2.yml file in the Fuse project.  

If deploying the application using Cloud Foundry, stdout/stderr should be used (see discussion under [Cloud Foundry](https://docs.google.com/document/d/1eNODztmE8VPgQ8sEs-QyGH5rjXgx_2esnp5cJK2YF7Q/edit#heading=h.xacw9frrjqed), below). An example of a log configuration file that accomplishes printing to stdout/stderr is shown in log4j2-stdout.yml.  

To explicitly use a certain log config file, set “-Dlog4j.configurationFile=my-log4j-file.yml” as a VM arg. Log4j will also attempt to automatically find log config files on the classpath if that property is not set - see the “Automatic Configuration” section of <https://logging.apache.org/log4j/2.x/manual/configuration.html>.  

### Single line exception logging
Exceptions that span multiple lines can be difficult to find because they can span across multiple documents and the first line of the stack trace has the trace id attached. Additionally, they are not guaranteed to be written to elastic search in the same order as they were output. If the whole stack trace is on one line, it makes it easier to find the full details.

To get exceptions to log to a single line, add ‘*%xThrowable{separator(|)}*'  to the logging Pattern in all log4j config files. This replaces new lines on logged exceptions with the pipe (|) symbol.


## Wit.ai
### Entities
Wit will analyze a user’s input sentence and return a JSON with the __entities__, and likely the __intent__ it discovers. An entity is any piece of text that we want Wit to identify and falls into a specific category. This could be a single word, or a string of multiple words. Each entity we define should have a number of examples that we specify. An __intent__ is a special type of entity. Instead of identifying a string within the request, Wit will try to associate a whole request to a single intent. For example the sentence “What’s for lunch?” would map to an intent, “lunch-questions”.  
We can define entities and intents in the Wit.ai web UI by going to the’ understanding’ tab and writing a sentence in the text box. From there we can assign an intent to the whole sentence, and apply entities to individual strings by highlighting them then adding the entity in the menu that shows up below the text box. The more examples we provide to Wit.ai for each intent and entity, the better it will be at identifying new examples in requests that it has not seen before.


### Handling Data
We can reach our Wit.ai app using an HTTP GET request. Wit will return a JSON with the following format:

```json
{
    "_text": "Where can I learn about java?",
    "entities": {
        "question_descriptors": [
            {
                "confidence": 1,
                "value": "learn",
                "type": "value"
            }
        ],
        "tech": [
            {
                "confidence": 1,
                "value": "java",
                "type": "value"
            }
        ],
        "intent": [
            {
                "confidence": 0.99099785686381,
                "value": "tech_question"
            }
        ]
    },
    "msg_id": "01DbjVd4akXZEuMWL"
}
```

We have “_text”, the actual request sentence, “msg_id”, an identifier, and “entities”. Entities is a collection of all the entities included in the request, including intent, if it identifies one. Using Feign and the Jackson converter, fuse takes these files and converts them to usable WitResponse objects with the following structure.

![image](https://drive.google.com/uc?export=view&id=1hZN4ehVNPbGhLNULNjGrUTf0SRtOuwHG)


## Custom exceptions: when and how


## Galatea coding standards

### Based on Google Style for Java
See <https://google.github.io/styleguide/javaguide.html>

### Extra rules
Method parameters must be final

### Checkstyle
There is a maven checkstyle plugin which will scan the codebase for any violations of the code style. This plugin will automatically run during the build as part of the `mvn validate` goal and can be manually run via `mvn checkstyle:check` . See the maven-checkstyle-plugin section of the pom.xml for more details of how checkstyle is configured, such as specifying a checkstyle rules file and excluding files from checkstyle validation.

### Sonar Quality Profile
Sonar is configured using the ‘Galatea Way’ quality profile. The Galatea Way is set as a child of the built-in ‘Sonar Way’ profile but with some additional Galatea customizations. Since it is a child, any changes to the Sonar Way should automatically be reflected in the Galatea Way. In case changes don’t automatically go through, Sonar Way rules can be manually imported into the Galatea Way:

![image](https://drive.google.com/uc?export=view&id=1aBHr7oBAkgVv76vr9NWcm1wk3bUshlX1)

## Protobuf
Protobuf support is implemented in Fuse in two places: a [JMS listener](https://github.com/GalateaRaj/fuse-starter-java/blob/develop/src/main/java/org/galatea/starter/entrypoint/SettlementJmsListener.java) processes protobuf messages sent to a queue, while [SettlementProtoRestController](https://github.com/GalateaRaj/fuse-starter-java/blob/develop/src/main/java/org/galatea/starter/entrypoint/SettlementProtoRestController.java) handles GET/POST requests consuming and returning Protobuf messages.

### Message Format
Protobuf messages are defined in .proto files. In Fuse there is a [ProtobufMessages.proto](https://github.com/GalateaRaj/fuse-starter-java/blob/develop/src/main/proto/ProtobufMessages.proto) file that defines the java package that the generated classes will be put in, the outer class name that will be generated and each of the messages.

The syntax for a message is:
```java
Message <message name> {
    <type> <field name> = <field id>
}
```
When a protobuf message is serialized, the field names and type information are omitted. A serialized protobuf message will be a sequence of field ids with their values, therefore it is imperative that once a field id is assigned to a particular field in a message, it is not changed.  
We generate Java classes to represent the messages using the [protoc compiler](https://developers.google.com/protocol-buffers/docs/javatutorial#compiling-your-protocol-buffers). The result will be a [Java file](https://github.com/GalateaRaj/fuse-starter-java/blob/develop/src/main/java/org/galatea/starter/entrypoint/messagecontracts/ProtobufMessages.java) containing an outer class with name matching the java_outer_classname parameter in the proto file and nested classes for each message type that extend `com.google.protobuf.GeneratedMessageV3`, implement the Builder pattern and encapsulate reading and writing of serialized protobuf messages.  

### JMS Listener
The JMS Listener functionality for protobuf is implemented in [SettlementJmsListener](https://github.com/GalateaRaj/fuse-starter-java/blob/develop/src/main/java/org/galatea/starter/entrypoint/SettlementJmsListener.java) where the `settleAgreementProto(byte[])` method, annotated with the `@JmsListener` attribute, handles messages received in that format.

The method expects that clients will send simple byte[] messages. Those messages are translated to the internal representation of trade agreements using an [implementation](https://github.com/GalateaRaj/fuse-starter-java/blob/develop/src/main/java/org/galatea/starter/ProtoMessageTranslationConfig.java) of the [ITranslator](https://github.com/GalateaRaj/fuse-starter-java/blob/develop/src/main/java/org/galatea/starter/utils/translation/ITranslator.java) interface that will perform the two steps of deserializing the byte[] into its Java representation and translating that to the internal object.

That allows processing to happen as normal. No special settings in Spring JMS are necessary and this uses the default `SimpleMessageConverter` to handle byte messages.

### HTTP Endpoint
In order to segregate the Protobuf dependencies from the rest of Fuse code, the REST endpoints for that message type are implemented in a separate controller class, [SettlementProtoRestController](https://github.com/GalateaRaj/fuse-starter-java/blob/develop/src/main/java/org/galatea/starter/entrypoint/SettlementProtoRestController.java).

Before using protobuf messages, the `ProtobufHttpMessageConverter` has to be added to the HTTP message converters. This is done in Fuse’s [implementation](https://github.com/GalateaRaj/fuse-starter-java/blob/develop/src/main/java/org/galatea/starter/MvcConfig.java) of the `WebMvcConfigurerAdapter`.

The REST endpoints in the controller are annotated with `@PostMapping` or `@GetMapping` that specifies the format of messages they produce and consume as *application/x-protobuf*.

Spring MVC will parse to/from the protobuf messages and deliver input parameters already in their Java representation, so in this case only the translation from the Protobuf message classes to the internal object representations is necessary. This is achieved using the [translator](https://github.com/GalateaRaj/fuse-starter-java/blob/develop/src/main/java/org/galatea/starter/MessageTranslationConfig.java) beans in the application. 


## CI builds
All code committed to GitHub and pushed to the cloud repository gets automatically built and the builds get validated.  

If the code is committed to a feature or bugfix branch out of develop, the code will be compiled and the unit test suite will be run against it. Any unit test failures will fail the build.  

For pull request or develop builds, the code will be built and unit tested, and subsequently deployed to Pivotal Web Services for integration and performance testing. Failures in tests on either of those suites will fail the build.  

### Jenkins
We use Jenkins for continuous integration. We found that together with TeamCity it is the most common solution for that type of workflow at client sites.

Our builds are defined in [declarative pipelines](https://jenkins.io/doc/book/pipeline/getting-started/), using their own DSL to specify the build stages and steps. The [code for the pipeline](https://github.com/GalateaRaj/fuse-starter-java/blob/develop/Jenkinsfile) is versioned in GitHub and stored in the root of the Fuse repository.

Pipeline builds stop at the first stage failure, so once a step fails to complete, all subsequent steps are skipped.

#### Pipeline stages
##### Build
Perform Maven’s __clean__ and __compile__ goals, performing an update of the local repository in the build agent and compiling all sources. This step is performed for all builds.
##### Unit Tests
Perform Maven’s __test__ goal and use Jacoco to generate test reports that will later be used for code coverage analysis. This step is performed for all builds.
##### SonarQube
Uses the SonarQube support for Jenkins pipelines and the Maven Sonar plugin to submit the execution reports and metadata to SonarCloud for analysis. This step only submits the data necessary to perform the analysis, but does not receive the response. This step is also performed for all builds.
##### Quality gate
Uses the waitForQualityGate() step to wait for the SonarCloud analysis to complete and check the results. It has a timeout of 2 minutes, so if SonarCloud does not finish analysis in that time the build is aborted. This step is performed for all builds.
##### Deploy
If the build is for a ‘deploy branch’ (pull requests, develop or master) the build will get automatically deployed to Pivotal Web Services (PWS) for integration and performance testing. This is done using the Cloud Foundry client tools to push the build artifacts to the server and the embedded manifest file that sets the deployed application name and other settings. This step is performed only for pull request, develop, and master builds.
##### Integration tests
Uses the Maven verify goal to execute the integration tests suite against the application hosted in PWS. This step is performed for pull request, develop and master builds.
##### Performance tests
This step is not currently implemented. The intention is that it will run automated performance tests against the deployed application.
##### Shutdown
Shuts down the application deployed to PWS and deletes it, together with its route. This is a clean up step that is performed regardless of build success or failure and is performed for pull request, develop or master builds.

### SonarQube vs. Checkstyle
There are multiple tools to support developers to create better code. Two such tools used are SonarQube and Checkstyle. These tools inspect code in different ways for slightly different purposes.  

SonarQube looks to provide code inspection to help detect issues within the code. This ranges from attempting to detect bugs, detect code smells and possible security vulnerabilities. These proposed issues can prove to be invaluable in ensuring automated reliability checks of developed code.  

The main issues SonarQube reports on are; duplicated code, coding standards, unit tests, code coverage, code complexity, comments bugs and security vulnerabilities.  

Another tool used is Checkstyle. Conceptually, Checkstyle is a simpler, and sometimes considered a “useless” tool. This, however, is not the case!  Checkstyle provides an automated way of checking for conventions, such as; naming, commenting and formatting. Although the issues raised by this tool do not provide ways of impacting stability, performance or reliability, it does, however, help to improve the codes future maintainability. It is a waste of a developers time trying to figure out what parts of the code provide which functionality, therefore by enforcing a set convention, can dramatically reduce that time, and allow the developer to focus on writing actual code.

### JaCoCo
When constructing unit tests, the goal is to test as much of the code as possible. However, it is often conceptually difficult to tell how much code is actually being checked by the unit tests. This is why JaCoCo is used.

JaCoCo stands for Java Code Coverage and provides a library to determine how much of the code is checked by unit tests. This shows how many; classes, methods, blocks and lines are covered by the tests, with the aim being to have 100% code coverage.

Do note, that 100% code coverage does not necessarily reflect effective testing, as it only reflects on the amount of code exercised during tests.

### Domain Objects

### Interacting with a Database
When deciding upon how to interact with a database, please consult the decision tree below:

![image](https://drive.google.com/uc?export=view&id=1T8C3moWYcJrtmTPGY-8GO9fXp3SqHT5I)


## Cloud Foundry
### Overview
FUSE is deployed to the Pivotal Web Services implementation of Cloud Foundry, a cloud-based application hosting platform-as-a-service (PaaS). Cloud Foundry allows for easily deploying and running app instances without having to manually manage application hosts. Developers can simply push an application to CF in an appropriate form, such as uber jar for a Java application, and CF will handle provisioning VMs and containers with the appropriate amount of resources and exposing a public URL for the app, if applicable.  

Apps deployed on Cloud Foundry do not have access to persistent local storage. When an application stops, any local files will be cleared. Logs should be written to stdout/stderr rather than to a file, and any data that needs to persist between restarts should be saved to some sort of external storage, such as a database or cloud file store.  

PWS also offers services that can be bound to apps to extend their functionality. For example, PWS offers database services that automatically create a cloud-hosted database that can be easily integrated with one or more deployed apps.  

Access to the FUSE organization on PWS CF can be granted by an admin (Raj, James, Paulo).

### Web UI
Pivotal Web Services has a UI at <https://console.run.pivotal.io/>. This UI provides a way to view and manage app deployments and PWS-offered services.

### CF CLI
The CF CLI can be installed from <https://docs.cloudfoundry.org/cf-cli/install-go-cli.html>, and allows for interacting with CF via the command line. A full list of commands can be found at <http://cli.cloudfoundry.org/en-US/cf/>. The important ones are:
* cf login -a api.run.pivotal.io - Log into PWS CF to allow running other commands
* cf logs [--recent] my-app-name - Tail application logs [or show recent logs]
* cf push -f my-manifest-file.yml - Push an app using the information defined in a manifest file, which contains information such as CF settings and app configuration
* cf restage my-app-name - Restart application, pulling in any config and service changes
* cf restart my-app-name - Restart application using the same config and services as the last restart, even if there are changes

### Usage
The three main ways we use CF are:
1. Running the develop version of FUSE
2. Running a PR version of FUSE for automated integration tests
3. Running a developer’s local version of FUSE for manual testing  

Usages 1 and 2 occur automatically as part of the CI build process. Usage 3 can be used as an alternative to running the application locally, and should be used at least once before submitting a PR to check that the code is able to successfully run on CF. If PWS services are added to FUSE, usage 3 may become the only way to test FUSE “locally”. A local version of FUSE can be pushed using `mvn clean package -Dskiptests && cf push -f manifest-local.yml`.

Note that since <https://github.com/GalateaRaj/fuse-starter-java/issues/137>, usages 1 and 2 deploy using a pseudo-manifest defined under the pushToCloudFoundry section in the Jenkinsfile. The Jenkinsfile manifest and manifest-local.yml should be kept in sync to reduce inconsistencies between local and automated deployments.
