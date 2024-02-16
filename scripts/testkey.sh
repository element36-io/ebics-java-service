#!/bin/bash

# Read the JSON file
json_file="backup.json"
contents=$(<"$json_file")

# Extract and decode the three blob fields
auth_blob=$(echo "$contents" | jq -r '.authBlob')
enc_blob=$(echo "$contents" | jq -r '.encBlob')
sig_blob=$(echo "$contents" | jq -r '.sigBlob')

# Convert base64 encoded keys to PEM format
echo "$auth_blob" | base64 -d > auth_public_key.pem
echo "$enc_blob" | base64 -d > enc_public_key.pem
echo "$sig_blob" | base64 -d > sig_public_key.pem

