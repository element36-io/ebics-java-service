#!/bin/bash
export IBAN=CH2108307000289537320
export BIC=HYPLCH22XXX
export EXTERNAL_IBAN=CH1230116000289537312
export EXTERNAL_BIC=HYPLCH22XXX
export REGISTERED_IBAN=CH2108307000289537313
export REGISTERED_BIC=HYPLCH22XXX

export CONNECTION_NAME=testconnection
export SECRET=backupsecret
export BACKUP_FILE=/app/scripts/backupfile
export EBICS_USER_ID=e36
export EBICS_HOST_ID=testhost
export EBICS_PARTNER_ID=e36
export LIBEUFIN_NEXUS_USERNAME=foo
export LIBEUFIN_NEXUS_PASSWORD=superpassword

client_pr_key=${CLIENT_PR_KEY_OUT:-/app/keys/client_private_key.pem}
client_pub_key=${CLIENT_PUB_KEY_OUT:-/app/keys/client_public_key.pem}
bank_pub_key=${BANK_PUB_KEY_OUT:-/app/keys/bank_public_key.pem}

sql="SELECT \"bankEncryptionPublicKey\" from nexusebicssubscribers s where \"hostID\"='testhost'"
echo $POSTGRES_PASSWORD
# PGPASSWORD=$POSTGRES_PASSWORD psql -d "$POSTGRES_DB" -h "$POSTGRES_HOST" -U "$POSTGRES_USER" -t -c "$sql" | xxd -r -p | openssl rsa -pubin -inform DER -outform PEM -out $bank_pub_key-enc
 PGPASSWORD=$POSTGRES_PASSWORD psql -d "$POSTGRES_DB" -h "$POSTGRES_HOST" -U "$POSTGRES_USER" -t -c "$sql" | xxd -r -p | openssl rsa -pubin -inform DER -outform PEM -out /app/keys/t-bankEncryptionPublicKey.pem

sql="SELECT \"bankAuthenticationPublicKey\" from nexusebicssubscribers s where \"hostID\"='testhost'"
echo $POSTGRES_PASSWORD
# PGPASSWORD=$POSTGRES_PASSWORD psql -d "$POSTGRES_DB" -h "$POSTGRES_HOST" -U "$POSTGRES_USER" -t -c "$sql" | xxd -r -p | openssl rsa -pubin -inform DER -outform PEM -out $bank_pub_key-enc
 PGPASSWORD=$POSTGRES_PASSWORD psql -d "$POSTGRES_DB" -h "$POSTGRES_HOST" -U "$POSTGRES_USER" -t -c "$sql" | xxd -r -p | openssl rsa -pubin -inform DER -outform PEM -out /app/keys/t-bankAuthenticationPublicKey.pem
