# Overview of Milestone deliveries

## Milestone 2 - Banking API Integration

- **Estimated duration:** 1 month
- **FTE:**  1.2
- **Costs:** 5,000 USD

| Number | Deliverable | Specification | Assets |
| -----: | ----------- | ------------- |- ----- |
| 0a. | License | Apache 2.0  | [Link to Licence](https://github.com/element36-io/ebics-java-client?tab=LGPL-2.1-1-ov-file) |
| 0b. | Documentation | We will provide both inline documentation of the code and a basic tutorial that explains how a user can start the backend and send test transactions, which will show how the new functionality works.| [Link to Documentation Root](https://github.com/element36-io/ebics-java-service?tab=readme-ov-file) |
| 0c. | Testing Guide | Adapt unit tests of core functions and test data to ensure functionality and robustness of the overall system (bridge and proofs). In the guide, we will describe how to run these tests.  | [Testing Guide](TEST.md)|
| 0d. | Docker | We will provide a Dockerfile(s) that can be used to test all the functionality delivered with this milestone. |[Testing Guide](TEST.md)|
| 1. | Generate Receipt | Retrieve data form backend and generate receipt (proof) using the proving system.  | [Testing Guide](TEST.md)|
| 2. | Provide API | We will provide an application based on Spring-Boot that will contain `getBankstatements():Statement[]` (which includes account balance),  `createOrder (OutgoingPayment)` and `simulatePayment(Payment)`  as a REST interface as [described](#b-bank-account-rest-api). The recipe data is added in the [backend API](http://w.e36.io:8093/ebics/swagger-ui/?url=/ebics/v2/api-docs/#/) `/ebics/api-v1/bankstatements`` with two new fields in the top level of the JSON-response: `recipeiUrl` so that clients can use a public verifier to check the proof. | [Testing Guide](TEST.md)|
| 3. | Provide Banking-UI | Show a UI to see the status of banking backend (show accounts, transactions etc). We will use [LibEuFin](https://github.com/element36-io/ebics-java-service/blob/main/docs/SANDBOX.md) - same as in our first grant. | [Testing Guide](TEST.md)|
| 4. | Unit Tests | We will adapt unit tests and test data to cover creating and validating proofs.  | [Testing Guide](TEST.md)|
| 5. | Repository | Repository will be the existing repo [ebics-java-service](https://github.com/element36-io/ebics-java-service)  | [Testing Guide](TEST.md)|
| 6. | Backend-Docker | Provide docker-compose images for setting up banking API wrapper, LibEuFin proxy for banking-API. Set up test data in the backend via script and run tests which include the proving system.  | [Testing Guide](TEST.md)|
| 7. | Swagger | Provide Swagger docu for the backend. | [Hosted Swagger](http://w.e36.io:8093/ebics/swagger-ui/?url=/ebics/v2/api-docs/#/) |


##DONE: Milestone 1 from [Hyperfridge](https://github.com/element36-io/hyperfridge-r0)


| Number | Deliverable | Specification | Assets |
| -----: | ----------- | ------------- | ------ |
| 0a. | License | Apache 2.0  | [Link to Licence](https://github.com/element36-io/hyperfridge-r0/blob/main/LICENSE) |
| 0b. | Documentation | We will provide both inline documentation of the code, a basic tutorial and a markdown description of the proof system. | [Link to Documentation Root](https://github.com/element36-io/hyperfridge-r0/blob/main/README.md)|
| 0c. | Testing Guide | Provide unit tests of core functions and test data to ensure functionality. In the guide, we will describe how to run these tests. | [Link to Testing Guide](https://github.com/element36-io/hyperfridge-r0/blob/main/docs/INSTRUCTIONS.md) |
| 0d. | Docker | We will provide a Dockerfile(s) that can be used to test all the functionality delivered with this milestone. | [Dockerhub](https://hub.docker.com/repository/docker/e36io/hyperfridge-r0/general) [Testing Guide](https://github.com/element36-io/hyperfridge-r0/blob/main/docs/INSTRUCTIONS.md) 
| 1a. | risc0 Guest Program | Code (circuit) to generate the proof, later used by the proving system. Secret input of [Guest Program](https://dev.risczero.com/terminology#guest-program): Ebics envelope as XML and Z53/Camt53 file(s) as ZIP binary - see XML examples above. The Public input is: Public Certificate of the Bank or name of bank, bank account number, balance and date. The [journal](https://dev.risczero.com/terminology#journal) will contain balance, currency, timestamp in the ebics-envelope, timestamp of the proof, client-account-number, Bank-ID and sequence number of the bank-statement. The circuit will check the hash of the (zipped) Z53 documents and compares it with the data given in the ebicsRequest. It checks the signature of the Ebics request and the signed hash of the ZIP file using crypto standards X002 and E002. "X002" refers to RSA signature key with a key length of 2048 bits, "E002" defines RSA algorithm for encryption using  ECB (Electronic Codebook) and PKCS#1 v1.5 padding.| [Repo](https://github.com/element36-io/hyperfridge-r0/tree/main/methods/guest)
| 1b. | Generate Receipt | Generate [receipt](https://dev.risczero.com/terminology#receipt) which proves that the computation (e.g. balance) is correct and signed by the bank.  | [Testing Guide](https://github.com/element36-io/hyperfridge-r0/blob/main/docs/INSTRUCTIONS.md)|
| 1c. | Validator | Code to validate the receipt. | [Repo](https://github.com/element36-io/hyperfridge-r0/tree/main/verifier)|
| 1d. | Hyperfridge Crate | The crate to create and validate recipes (ZKPs), wrapping the functionality.  | [Repo](https://github.com/element36-io/hyperfridge-r0/tree/main/host)
| 2. | Unit Tests | We will add unit tests and test data for creating and validating proofs which includes edge cases like wrong balance claims or faulty signature of the bank. | [Testing Guide](https://github.com/element36-io/hyperfridge-r0/blob/main/docs/INSTRUCTIONS.md)  |
| 3. | Performance Benchmark | Present a table with performance metrics, so that hyperfride proofing times can be interpolated with data from [risc-zero](https://dev.risczero.com/datasheet.pdf). | [Benchmarks](runtime.md) |