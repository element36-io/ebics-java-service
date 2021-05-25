## Run unit tests

Run tests for the ebics-java-client on linux - it mounts sources into a docker container with java and the maven build tool

    git clone  https://github.com/element36-io/ebics-java-service.git
    cd ebics-java-client; mkdir ./app;
    docker run -it -v $PWD:/app -w /app  maven:3-jdk-8 mvn test surefire-report:report

See `./target` for test results. `surefire-report:report` is optional but it creates test report here: `./target/site/surefire-report.html`

With minimum Java 8 and Maven run tests directly with `mvn test surefire-report:report`

See [here](https://github.com/element36-io/ebics-java-client/blob/master/README.md) how to run tests on ebics-java-client. 

## Start the API and test manually

    docker run -p 8093:8093 e36io/ebics-service

Open [Swagger](http://localhost:8093/ebics/swagger-ui/?url=/ebics/v2/api-docs/) in your 
browser and test the `simulate` service. You may check and download the payment document (ebics document) 
which can be tested manually against your bank. Many banks offer an upload service for transations. 
Other APIs need an online connection to your bank, but the default is set so simulate an banking 
interface. To set up and connect to your banks Ebics API you need to [switch to productive spring boot
profile](https://www.baeldung.com/spring-profiles) by using `export spring_profiles_active=prod`. 
