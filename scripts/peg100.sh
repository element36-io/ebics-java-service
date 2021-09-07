#!/bin/bash

curl -X 'POST' \
  'http://localhost:8093/ebics/api-v1/createOrder' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "amount": 100,
  "clearingSystemMemberId": "HYPLCH22",
  "currency": "EUR",
  "msgId": "emtpy",
  "nationalPayment": true,
  "ourReference": "empty",
  "pmtInfId": "empty",
  "purpose": "0x9A0cab4250613cb8437F06ecdEc64F4644Df4D87",
  "receipientBankName": "Hypi Lenzburg AG",
  "receipientCity": "Baar",
  "receipientCountry": "CH",
  "receipientIban": "CH1230116000289537313",
  "receipientName": "element36 AG",
  "receipientStreet": "Bahnmatt",
  "receipientStreetNr": "25",
  "receipientZip": "6340",
  "sourceBic": "HYPLCH22",
  "sourceIban": "CH2108307000289537320"
}'