#!/bin/bash



curl -X 'POST' \
  'http://localhost:8093/ebics/api-v1/createOrder' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "amount": EUR:10000,
  "clearingSystemMemberId": "HYPLCH22XXX",
  "currency": "EUR",
  "msgId": "emtpy",
  "nationalPayment": true,
  "ourReference": "initial 10kEUR",
  "pmtInfId": "empty",

  "purpose": "5FLSigC9HGRKVhB9FiEo4Y3koPsNmBmLJbpXg2mp1hXcS59Y",
  "receipientBankName": "Hypi Lenzburg AG",
  "receipientCity": "Baar",
  "receipientCountry": "CH",
  "receipientIban": "CH2108307000289537320",
  "receipientName": "element36 AG",
  "receipientStreet": "Bahnmatt",
  "receipientStreetNr": "25",
  "receipientZip": "6340",
  "sourceBic": "HYPLCH22XXX",

  "sourceIban": "CH1230116000289537313"

}'