#!/bin/bash


echo "start\n"

function send() {
	printf "\n create order from:$from, to:$to, amount:$amt."
	curl -X 'POST' \
	  'http://localhost:8093/ebics/api-v1/createOrder' \
	  -H 'accept: */*' \
	  -H 'Content-Type: application/json' \
	  -d '{
	  "amount": "'"$amt"'",
	  "clearingSystemMemberId": "HYPLCH22XXX",
	  "currency": "EUR",
	  "msgId": "emtpy",
	  "nationalPayment": true,
	  "ourReference": "transfer $amt",
	  "pmtInfId": "empty",
	
	  "purpose": "5FLSigC9HGRKVhB9FiEo4Y3koPsNmBmLJbpXg2mp1hXcS59Y",
	  "receipientBankName": "Hypi Lenzburg AG",
	  "receipientCity": "Baar",
	  "receipientCountry": "CH",
	  "receipientIban": "'"$to"'",
	  "receipientName": "element36 AG",
	  "receipientStreet": "Bahnmatt",
	  "receipientStreetNr": "25",
	  "receipientZip": "6340",
	  "sourceBic": "HYPLCH22XXX",
	  "sourceIban": "'"$from"'"
	}'
}


function statement() {
	printf "\nget stmt ==> \n"
	curl -X 'GET' \
	  'http://localhost:8093/ebics/api-v1/bankstatements' \
	  -H 'accept: */*'
	printf "\n======================================================="
 }
 
 function unpeg() {
	printf "\n unpeg $amt"
	 curl -X 'POST' \
	  'http://localhost:8093/ebics/api-v1/unpeg' \
	  -H 'accept: */*' \
	  -H 'Content-Type: application/json' \
	  -d '{
	  "amount": "'"$amt"'",
	  "clearingSystemMemberId": "HYPLCH22XXX",
	  "currency": "EUR",
	  "nationalPayment": true,
	  "ourReference": "empty",
	  "purpose": "0x9A0cab4250613cb8437F06ecdEc64F4644Df4D87",
	  "receipientBankName": "Hypi Lenzburg AG",
	  "receipientCity": "Baar",
	  "receipientCountry": "CH",
	  "receipientIban": "'"$to"'",
	  "receipientName": "element36 AG",
	  "receipientStreet": "Bahnmatt",
	  "receipientStreetNr": "25",
	  "receipientZip": "6340"
	}'
 }
 
statement
from="CH1230116000289537313"; to="CH2108307000289537320"; amt="100";
send
from="CH1230116000289537312"; to="CH2108307000289537320"; amt="100";
send
statement
printf "\n --> from 20"
from="CH2108307000289537320"; to="CH2108307000289537312"; amt="100";
send
from="CH2108307000289537320"; to="CH2108307000289537313"; amt="100";
send
to="CH2108307000289537313"; amt="100";
unpeg
statement
#
to="CH2108307000289537312"; amt="100";
unpeg
statement

#/bank-accounts/CH2108307000289537320/transactions
#/bank-accounts/CH2108307000289537320/payment-initiations



