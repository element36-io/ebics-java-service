#!/bin/bash
# Commands taken from
# https://docs.taler.net/libeufin/nexus-tutorial.html#installing-libeufin

# exit on error
set -e

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
# export LIBEUFIN_SANDBOX_USERNAME=$LIBEUFIN_NEXUS_USERNAME
# export LIBEUFIN_SANDBOX_PASSWORD=$LIBEUFIN_NEXUS_PASSWORD



# wait for DB to be initialized
until PGPASSWORD=$POSTGRES_PASSWORD psql -d "$POSTGRES_DB" -h "$POSTGRES_HOST" -U "$POSTGRES_USER" -c'\q'; do
  >&2 echo "Postgres is unavailable - sleeping"
  sleep 1
done

# We actually do not need nexusserver, but the UI need the nexus server:
echo ... start nexus
libeufin-nexus serve --port 5000 --host 0.0.0.0 &

echo starting sandbox
libeufin-sandbox serve --port 5016 --auth &

echo check if sandbox is up
until libeufin-cli sandbox check; do
  >&2 echo "Sandbox is unavailable - sleeping"
  sleep 1
done



if [ ! -f "/app/initdone" ]; then

    echo ... create hostid
    libeufin-cli sandbox ebicshost create --host-id $EBICS_HOST_ID
    echo list
    libeufin-cli sandbox ebicshost list

    echo ... create user $LIBEUFIN_SANDBOX_URL 
    libeufin-cli sandbox  --sandbox-url $LIBEUFIN_SANDBOX_URL/demobanks/default demobank register

fi

echo list ebicssubscriber
libeufin-cli sandbox ebicssubscriber list
echo list bankaccount
libeufin-cli sandbox bankaccount list

echo ...setup nexus
echo connname $CONNECTION_NAME
echo url $LIBEUFIN_NEXUS_URL 
echo username $LIBEUFIN_NEXUS_USERNAME 
echo pw $LIBEUFIN_NEXUS_PASSWORD
echo base $EBICS_BASE_URL
echo hostid $EBICS_HOST_ID
echo partnerid $EBICS_PARTNER_ID
echo userid $EBICS_USER_ID


 #  create sandbox accounts
if [ ! -f "/app/initdone" ]; then

    echo ... create sanbox subscribed user
    libeufin-cli sandbox \
        --sandbox-url $LIBEUFIN_SANDBOX_URL/demobanks/default \
        demobank new-ebicssubscriber \
        --host-id $EBICS_HOST_ID --partner-id $EBICS_PARTNER_ID --user-id $EBICS_USER_ID \
        --bank-account $IBAN    \
        --bank-account $LIBEUFIN_SANDBOX_USERNAME

fi

if [ ! -f "/app/initdone" ]; then


    echo list ebicssubscriber
    libeufin-cli sandbox ebicssubscriber list
    echo list bankaccount
    libeufin-cli sandbox bankaccount list

    echo ... create superuser
    until libeufin-nexus superuser $LIBEUFIN_NEXUS_USERNAME --password $LIBEUFIN_NEXUS_PASSWORD; do
        >&2 echo "Nexus is unavailable - sleeping"
        sleep 1
    done

    echo ...new connection
    libeufin-cli \
        connections \
        new-ebics-connection \
            --ebics-url $EBICS_BASE_URL \
            --host-id $EBICS_HOST_ID \
            --partner-id $EBICS_PARTNER_ID \
            --ebics-user-id $EBICS_USER_ID \
            $CONNECTION_NAME

    echo ... backup
    libeufin-cli  connections  export-backup --passphrase $SECRET   --output-file $BACKUP_FILE  $CONNECTION_NAME        

    # This syncronization happens through the INI, HIA, and finally, HPB message types

    echo ... keyletter
    libeufin-cli connections get-key-letter $CONNECTION_NAME /app/letter.pdf

    echo ... connect
    libeufin-cli \
        connections \
        connect \
            $CONNECTION_NAME


    echo ... create external account 2 $EXTERNAL_IBAN
    libeufin-cli sandbox ebicsbankaccount create \
        --iban $EXTERNAL_IBAN \
        --bic $EXTERNAL_BIC \
        --person-name "external account" \
        --account-name $EXTERNAL_IBAN \
        --ebics-host-id $EBICS_HOST_ID \
        --ebics-user-id $EBICS_USER_ID \
        --ebics-partner-id $EBICS_PARTNER_ID 

    echo ... download
    libeufin-cli \
        connections \
        download-bank-accounts \
            $CONNECTION_NAME     

    echo ... import acccount 2 to nexus db $EXTERNAL_IBAN
    libeufin-cli \
        connections \
        import-bank-account  \
            --offered-account-id $EXTERNAL_IBAN \
            --nexus-bank-account-id $EXTERNAL_IBAN \
            $CONNECTION_NAME
   
     echo ... create external account 3 $EXTERNAL_IBAN


    ## registered external account        
    libeufin-cli sandbox ebicsbankaccount create \
        --iban $REGISTERED_IBAN \
        --bic $REGISTERED_BIC \
        --person-name "registered external account" \
        --account-name $REGISTERED_IBAN \
        --ebics-host-id $EBICS_HOST_ID \
        --ebics-user-id $EBICS_USER_ID \
        --ebics-partner-id $EBICS_PARTNER_ID 


    echo ... download
    libeufin-cli \
        connections \
        download-bank-accounts \
            $CONNECTION_NAME    

    echo ... import acccount 3 to nexus db $REGISTERED_IBAN
    libeufin-cli \
        connections \
        import-bank-account  \
            --offered-account-id $REGISTERED_IBAN \
            --nexus-bank-account-id $REGISTERED_IBAN \
            $CONNECTION_NAME

    echo ... create sanbox account 1 $IBAN
    libeufin-cli sandbox ebicsbankaccount create \
        --iban $IBAN \
        --bic $BIC \
        --person-name "pegging account" \
        --account-name $IBAN \
        --ebics-host-id $EBICS_HOST_ID \
        --ebics-user-id $EBICS_USER_ID \
        --ebics-partner-id $EBICS_PARTNER_ID    

    echo ... download
    libeufin-cli \
        connections \
        download-bank-accounts \
            $CONNECTION_NAME                  

    echo ... import acccount 1 to nexus db $IBAN
    libeufin-cli \
        connections \
        import-bank-account  \
            --offered-account-id $IBAN \
            --nexus-bank-account-id $IBAN \
            $CONNECTION_NAME


    echo ... list offered bank accounts
    libeufin-cli \
        connections \
        list-offered-bank-accounts \
            $CONNECTION_NAME            

fi

touch /app/initdone
ls -la /app/initdone

#  install versions according to LibFinEu/frontend/README.md

echo list ebicssubscriber
libeufin-cli sandbox ebicssubscriber list
echo list bankaccount
libeufin-cli sandbox bankaccount list
echo list list-connections
libeufin-cli connections list-connections 

echo list show-connection
libeufin-cli connections show-connection $CONNECTION_NAME  


# apt update --allow-releaseinfo-change
# apt-get install jq -y # qpdf xxd libxml2-utils openssl -y
# client_pr_key="${CLIENT_PR_KEY:-/app/scripts/client_private_key.pem}"
# client_pub_key="${CLIENT_PUB_KEY:-/app/scripts/client_public_key.pem}"

cat /app/scripts/backupfile | jq -r '.sigBlob' | openssl enc -d -base64 -A | openssl pkcs8 -inform DER -outform PEM -out $client_pr_key  -passin pass:$SECRET
openssl rsa -pubout -in $client_pr_key -out $client_pub_key
echo "client pk exported to $client_pr_key public key to $client_pub_key "

sql="SELECT \"encryptionPrivateKey\" from nexusebicssubscribers s where \"hostID\"='testhost'"
# read hex key  | convert to binary | create pem
PGPASSWORD=$POSTGRES_PASSWORD psql -d "$POSTGRES_DB" -h "$POSTGRES_HOST" -U "$POSTGRES_USER" -t -c "$sql" | xxd -r -p x.txt | openssl pkcs8 -topk8 -nocrypt -inform DER -out /app/scripts/client_encryptionPrivateKey.pem
openssl rsa -in encryptionPrivateKey.pem -pubout -out /app/scripts/client_encryptionPublicKey.pem

sql="SELECT \"bankAuthenticationPublicKey\" from nexusebicssubscribers s where \"hostID\"='testhost'"
# read hex key  | convert to binary | create pem
PGPASSWORD=$POSTGRES_PASSWORD psql -d "$POSTGRES_DB" -h "$POSTGRES_HOST" -U "$POSTGRES_USER" -t -c "$sql" | xxd -r -p x.txt | openssl pkcs8 -topk8 -nocrypt -inform DER -out /app/scripts/bank_authenticationPublicKey.pem

echo "key generation done"

libeufin-cli accounts task-schedule --task-name=fetch-statement --task-type=fetch --task-cronspec="30 * * * *" --task-param-range-type=all --task-param-level=statement  $IBAN
libeufin-cli accounts task-schedule --task-name=fetch-statement --task-type=fetch --task-cronspec="30 * * * *" --task-param-range-type=all --task-param-level=statement  $EXTERNAL_IBAN
libeufin-cli accounts task-schedule --task-name=fetch-statement --task-type=fetch --task-cronspec="30 * * * *" --task-param-range-type=all --task-param-level=statement  $REGISTERED_IBAN

echo " auto fetch regitered"

# read -t 10 -p "Setup & startup of nexus and sandbox complete, starting Libeufin react-ui UI on localhost:3000, login with:  $LIBEUFIN_NEXUS_USERNAME $LIBEUFIN_NEXUS_PASSWORD "
#serve -s build
# /app/scripts/peg100.sh
# /app/scripts/test1.sh
echo "...starting yarn"
echo versions of yarn npm node
yarn --version
npm --version 
node --version
cd /app/frontend/ 
yarn --verbose start

