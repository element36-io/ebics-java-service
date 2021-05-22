## Overview

This project wraps a [java-based command-line tool](https://github.com/element36-io/ebics-java-client)
into a REST service. It can connect with your bank using 
[Ebics](https://en.wikipedia.org/wiki/Electronic_Banking_Internet_Communication_Standard) 
standard.

Setting it up successfully means, that you can 
- Read daily statements including account balance and transactions of multiple accounts
- Trigger payments from your bank account(s)

## Start the service & quickly take a look at the API

```
docker run XXX
```
Open [localhost:8093](http://localhost:8093/XXX in your Browser to access Swagger.


## Setup
 
Ebics (Electronic Banking Internet Communication Standard) defines the protocol and a series of document to access and exchange banking data. 
You need to talk to your bank first and request to provide access to the Ebics interface.
Alternatively you can ask about ISO20022 or an interface to transfer Camt.053 and Pain.001 or Pain.002
files, which should be well known among banks. Most banks offer the service for free. 

A crucial part to understand of Ebics is the key-exchange- roughly it works like this:  

- Generate keypair and send public to bank with the ebics client
- Bank sends their client specific key via a physically printed letter to you
- Add this key to the client 
- All configurations for the setup are done in one specific file: $HOME/ebics/ebics.txt

 Check [ebics.org](https://www.ebics.org/en/home) for details. 

![How Ebics works](https://miro.medium.com/max/984/1*0z3e_v3qErc4tF1a5JxhEg.png "How Ebics works")


## Supported banks and national flavours

Ebics standard was initiated by banks German/French partnership later adopted by Austria and Switzerland
and also by Banks across Europe - you will find many bank supporting Ebics, a general list of all
banks supporting Ebics was not found. 

There are national flavours regarding the data on bank transfers and bank statements, which are
represented by national changes of the original Ebics XML documents. For example Switzerland: 
- In Switzerland in addition to a reference field (e.g. invoice 1234) you may use a 
  "reference number" on payment slips  which you don't find in other countries
- Account statements, status reports are in a ZIP container (zipped).

With the homogenization of European banking interfaces (SEPA, ISO20022) you may use a common and minimal 
set information for working with bank interfaces which is reflected by the REST interface of this
project. Anyway you might need to adapt to local flavour of your country or your bank, which is 
can be done in the 'io.element36.cash36.ebics.strategy.*' package. 

If you need to adapt the code,
look for platforms which support testing your Ebics documents - there are national platforms, 
 banks and IT providers - see Tables and Links below or www.ebics.ch, www.ebics.de. www.ebics.at, www.ebics.org

### Switzerland

A selection of test interfaces for Ebics and test environments in Switzerland: 

| Bank  | URL for testing/validating | more info | 
| SIX Group | https://validation.iso-payments.ch/  | http://www.six-interbank-clearing.com/de/home/standardization/iso-payments/customer-bank/implementation-guidelines.html |
| Credit Suisse  | https://credit-suisse.com/iso20022test | https://iso20022test.credit-suisse.com/help |
| PostFinance | https://isotest.postfinance.ch/corporates/ ||
| Raiffeisen  | http://raiffeisen.ch/testbank ||
| UBS  | https://ubs-paymentstandards.ch/login | https://www.ubs.com/ch/de/swissbank/unternehmen/zahlungsverkehr/harmonisierung/testplattform-iso-20022.html ||
| ZKB  | https://testplattform.zkb.ch/ | https://testplattform.zkb.ch/help |

### Other countries

Besides France, Germany, Austria and Switzerland many other countries are covered with an Ebics- Service. 

Non-exhaustive examples of European banks: 

- [BNP Paribas](https://cashmanagement.bnpparibas.com/our-solutions/solution/global-ebics)
- [Santander Cash Nexus](https://www.santandercashnexus.com/information_en.html)
- [DZ Bank](https://firmenkunden.dzbank.de/content/firmenkunden/de/homepage/leistungen/Zahlungsverkehr/zugang_zum_konto/ebics.html)
- [Sparkasse](https://www.sparkasse.de/unsere-loesungen/firmenkunden/electronic-banking/online-banking-ebics.html)
- [Bank Austria](https://www.bankaustria.at/files/EBServices_23082013_final.pdf)
- [Hypo Vorarlberg](https://www.hypovbg.at/firmenkunden/digital-banking/sicherheit/ebics)

## Adapt to national flavours 

The setup process does not need to be adapted. You may want to adapt 
- the conversion between Ebics-XML documents: Adapt e.g. `io.element36.cash36.ebics.strategy.implStatementCamt53_001` for processing of daily statements.
- add new commands to ebics-java-cli: look for `org.kopi.ebics.interfaces.OrderType` which hold commands available at the command line but
 which are also directly transferred to the Ebics server as commands. An quick start is to look for national mapping tables if you face this issue. 

Examples: 

- [Switzerland](https://www.six-group.com/dam/download/banking-services/interbank-clearing/en/standardization/ebics/mapping-table.pdf)
- [Austria](https://www.stuzza.at/de/download/ebics/418-btf-mappingtabelle-at-v20210506.html)

## Run tests

Run tests for the ebics-java-client on linux - it mounts sources into a docker container with java and the maven build tool:

```
git clone git@github.com:element36-io/ebics-java-client.git
cd ebics-java-client
docker run -it -v $PWD:/app -w /app  maven:3-jdk-8 mvn test surefire-report:report

```
See ./target for test results. `surefire-report:report` is optional but it creates test report here: ./target/site/surefire-report.html  


See [here](https://github.com/element36-io/ebics-java-client/blob/master/README.md) how to run tests on ebics-java-client. 



## Kudos and references: 

Ebics-java-client is based cloned from https://github.com/uwemaurer/ebics-java-client




