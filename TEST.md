## Run unit tests

Run tests for the ebics-java-client on linux - it mounts sources into a docker container with java and the maven build tool

    git clone git@github.com:element36-io/ebics-java-client.git
    cd ebics-java-client; mkdir ./app;
    docker run -it -v $PWD:/app -w /app  maven:3-jdk-8 mvn test surefire-report:report

See `./target` for test results. `surefire-report:report` is optional but it creates test report here: `./target/site/surefire-report.html`


See [here](https://github.com/element36-io/ebics-java-client/blob/master/README.md) how to run tests on ebics-java-client. 

## Start the API and test manually

    docker run -p 8093:8093 e36io/ebics-service

Open [Swagger on localhost:8093](http://localhost:8093/ebics/swagger-ui/?url=/ebics/v2/api-docs/) in your 
browser and test the `simulate` service. For other APIs you need to set up and connect to your
banks Ebics API. 



