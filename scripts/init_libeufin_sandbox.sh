#!/bin/bash
# Commands taken from
# https://docs.taler.net/libeufin/nexus-tutorial.html#installing-libeufin

# exit on error
set -e

export IBAN=CH2108307000289537320
export BIC=HYPLCH22XXX
export EXTERNAL_IBAN=CH1230116000289537312
export EXTERNAL_BIC=HYPLCH22XXX
export REGISTERED_IBAN=CH1230116000289537313
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

echo versions of yarn npm node
yarn --version
npm --version 
node --version
cd /app/frontend/ 

Document(pdfDoc).use {
            it.add(Paragraph("Signaturschlüssel").setFontSize(24f))
            writeCommon(it)
            it.add(Paragraph("Öffentlicher Schlüssel (Public key for the electronic signature)"))
            writeKey(it, ebicsSubscriber.customerSignPriv)
            it.add(Paragraph("\n"))
            writeSigLine(it)
            it.add(AreaBreak())

            it.add(Paragraph("Authentifikationsschlüssel").setFontSize(24f))
            writeCommon(it)
            it.add(Paragraph("Öffentlicher Schlüssel (Public key for the identification and authentication signature)"))
            writeKey(it, ebicsSubscriber.customerAuthPriv)
            it.add(Paragraph("\n"))
            writeSigLine(it)
            it.add(AreaBreak())

            it.add(Paragraph("Verschlüsselungsschlüssel").setFontSize(24f))
            writeCommon(it)
            it.add(Paragraph("Öffentlicher Schlüssel (Public encryption key)"))
            writeKey(it, ebicsSubscriber.customerSignPriv)
            it.add(Paragraph("\n"))
            writeSigLine(it)
        }
        pdfWriter.flush()
        return po.toByteArray()
    }
    pe" : "ebics",
libeufin-1       |   "userID" : "e36",
libeufin-1       |   "partnerID" : "e36",
libeufin-1       |   "hostID" : "testhost",
libeufin-1       |   "ebicsURL" : "http://localhost:5016/ebicsweb",
libeufin-1       |   "authBlob" : "MIIE6TAbBgoqhkiG9w0BDAEDMA0ECKXKuNLZOXGCAgEeBIIEyE0salo6X/PtsTrw0NhFEE3VxenQYTqtjOazJSwEQvyAqq2HukvzuBseftNSULta6G/uBSF/EwFz02HYjCtFW68ViU+80nJqitXozS/ocEok2CiuGeJHqy77iqONPWOnGrBmNin0RXwQIzox+WiUx6w7lcAlv2GS/uac7UXm5E9gIlQ7JZDOa6PDIY3dtg26z1f3e//wWDgS7TMAt28EqHNq69frH+ZOCNw7yv0y5RI12IR2zgvcKDYHbGl3w0a0mnwKmh8far56qal/NLTAhvGboXh17FkyqkIDuuLYEJOPGXUm14+82jR47B6rEq6JC328oV/ZuEfrVECG+FWInuCmE/MD9D1GaXhk2R5iBaNHB5+BSdvaCNZNbM985Utkkb45oTiPgxGH9D/zrKDrGKUaYCWnM5R7CLtzH+1gcEjFoGsd4+/s35s6pk/YXIWeiOuGU3DHfZumYHk4343dfo5VMQADuBiOZKgR1owyqOok8aN/X8xoe3Q5plI1D7oaHr1Wp7ucRa9pUkO6cHcSYIxFxBIXFqlSRRIAn/HHhhvuhgQETSWP6A2JNToT1zTu9vq6aLfV1eF85vY4oiFzLBTY+tsHE2qLnxuuhYe/wZ7ToGUu8ozTBC7GePH0eYi9QibH1EC5UfBYCiQPMXstEyUDTzmi6YnfZsfc2v3R6KD+69p1YUwJu5UvLRKAcb9bRkRb2Do0ECJjWlAXRbeT0yh2UhfCze8/oQvuKXFQAnINt0phBRYXIxaNCvMFmNmdhSiIEr3mGbYmtPDJ51mVLvHdYw0VSXFxqvxo3/eDQs8IwtKP4cZVCND0lVsXMQhQWmBNf/nksnnEhPoKTbrF1Cb7vjVZUW2HqrC5xRbosQFJ6Bn2fJzU/Z0azSL7UtiaXKEbZz9CP/n0PeCBI3KlrC0ZhWAhe2eB3M7eLO0NpJ3QH8GjceB/Zx+QZ08OsdBV9FLjJKwS6oX6aCGQkmV7GOQmS5uzNJL5neL0JcOTxJDRqxI5EeZNv8Icx6sVh+XFHDe/W1zTZYpCoXsWrCvN2ZOGC4D2SLML2kl84+M/arVwLxMhpeqsP/QBvNvj+CQ8VN1gNEMXwR173hbGrNm6857f3f0qbD6JiIezGWMVdfzkpTjsu0RR4k+jaXQb1snEut8BSgTkFnknKRQuKbCqM6TGDdqmwia6b8Dc9xtPHOoM8e3NW/71rUKDpGqa5ir+zg886IyY9Ww64o3vsz9wiNP/jA7DzJJT8jBFxZDMTUK5kQMkurz6VDvCA1LiFrXFreNv7v7Ig6IozwSmdYF6c6m5EcVNsMJvIQjL3InaqcNVuI+1KhiWYLd6Ff/FT66U7nUIdJ3iaT5UulA8f9jVaO9eLKcKYqNP0T3OOGklE/4TJyKz13KQXUyRI2aw+lVKyJ2UkN0K++bokCzNtRSDTPosrKvPJCLPGMG8da3gj4rK63dj24qe+8ZGCHTNbporGN8647YoJkagtsxu7HUoZRL5lNQuWdTYqeCef/UqrplKJtgcy+0MGb99j+E7Vni3Iy+8q4caRoceUf6AmbNQRwY3akYcT1jcNkh7WVMcOr98y092+ex1lAcVgWxrzlhJlj8/D8pzjtFuMR2W46YTkz0JRaUDkyijNg==",
libeufin-1       |   "encBlob" : "MIIE6TAbBgoqhkiG9w0BDAEDMA0ECGF/ScME6IQTAgEeBIIEyGldiP5QeZ/vb8PSHcj9crGUaqDoZjmZ3vDxJo7s2hvepaP/hPLL8jVKT0wllS8y74GugFux0jMv+OuF00s1NzM+QE7vetuKMyHKfts5wF8YgbO0r8ENgsvKiSNWvFiUPocnhSWMl8zx8aX3GFjMecZCI7nZ4b1mkArjJS9TCao7tAyszE8+ww1/C8qrHqrNwoltMsBMhTStCjVEDmYaL45E39EnS6j0EqK0+fXA+cLAVDxBa3n8N1Osc19Am+rg5yA8bWa0ci16SgU3EL1vIjRFgvkBKPNYXy8BGUxxdqoXvJKgNKPordFSp0ygdu0HJrbwLcwPCO94UYrDqQY73Hwc9P8LwgerhFUVp0h3lK9Q9vq++bBemxaDWhWnl0LUG3K+sevUESlLArn/9lTmWBTeMkCV34JRedFpm5nuyKgBzlPjDsMjK8W2mjxLOcFovfCTcoLx8f7BLNP5voMhL+lBhNLOYsTvL/5LeBtYVsg0wATgR4xBr+wJ3NdlprwllEC221eBV6ImQjRS6VpieiC5pV1dPqWB6ex2ZCPbYbXpMiCbAYkSiDouatykfxEqXtYaz7wP+9hJxShaew7KhXDXzBeVkXMHGiurfkKR3/nmNS4mbKTMwG6l0bTZsmahSJGfNjVRKyQBs3EqYvh7sbI1LQDL9TwklqXgWIeGDCLsxB3ZIVcuzcCyWB3N8r8addo7+OuzR3pHkR9AfmUHt2afnPh8uqdsZezQvRqra3GHt7RIDYQw+RgxaSlW4StXQpd+sBRONn01LmzB3QaV+oACqgOI+As61t1XesPdDTf3BT4Jcp/qAUsfUzeOoEEOm9XNpM1hM2vWR9Q95dqoyi1DAttbPU2iCtXePzH+2YQA1wQ3+9Wk6s8zXTcNmjkm+rTingtmqTkkZjWjJlhkURXd1coIESAOQQ1Uw4fiyk2kvdG0R4L57kxvmzdQhocEvQJB8VwEODta+Lx5nGudUWtoH7dGORNZYiLxmbch094uX5ggEQjh0NyrtOeFgwJMCarvzwaTbNiOmFRc7+Ioul189gORn6sRhBI6gyYCCr0HO+J3p9pLo861kMhSSwQeC6/nYN9zRzJKBe38mWF0oToKiYi5Hk5BOvNnUKgiXgI80W1dBRKbd4zlD/9ADEOrKHAUfD0Fxl7nXtC8LBpWepYxqSX4GEFTXQd9C2jUg7mZVB6sUVaRZmvIlL21yF/Q1z5W9OZ+ipQrrp5+PMfFgCPisCOTbeKFt9gY9In2b/e9mUZWYixWvzx5mKTQ0s/Inm0A6Tql6Kx8b2pscZmZWji3c0rF8bTamk6jgJrRZwHqc2JMbS5iIcrLHUJTU8JNGDHilAmds2pngRGgG6lcmBhEp0s8bHT1MY4JmAyiBLigh45GWg3My/U+D6CTEAE58KXwLNIwd8ktb1O3zTo4RbZBLNNkbqAseOAHbIMsH9JX35W5oqpctmcZVI7piu+Sozj79p7e8U3qJspiYx4H1ATDum/eBn1d4G3CkSPe9oBDLfQ6+eAOIrpJIr+ZO9E2U4S7FSJG5OFVvuyB7V0sBOBEguq7tr8DYHUgHOxJRfrK7i6t416LnOR5YTj/vDVhvcO4pywBoInIU/XfbGOxEsWkA3V9z5yFxA==",
libeufin-1       |   "sigBlob" : "MIIE6TAbBgoqhkiG9w0BDAEDMA0ECB7l4nCsN5pFAgEeBIIEyOQB2uoLLimzY+hpT3kvWXXSYdO4+YMbwmy0Xq3dgc9TkNkol3Tpm2y0A93zC9Kw+6h7KG68MRZpQLuAS5N8UFxywMzxmimhhYWtMHt42KPy3R783TvAfnzb98z5e8uVFPos5qpk/5YUaGmnsj4iE4/t6zAE9fy98GSpQlZKOvQx8OmOE24iK7BpEvW5tQ622Q83D601zW20kaOlDvntJPADvdBGy1THJQzRSuf+VQxwj3TxGOHszyJgl/Eu+/DHjrgpEX2gTCoBv4PSnVkBd98h9QNoKyw/Bw74Y0gM5I6htbkOy4eTOii5DnS8wvCuOZY1TmzmHbKbkezXiQEMaZAMo+WpB9OFxttpPFqr0nG7Ra3MsID7JLc8/jnJcEB3iiIsj7U6nnsLjCPLKXwEffLpkGtlykctL0hKZi/m6udkNC4hOtbF3Ug3hd9gLkwBooBLuX/MD/rq2xnhUBjATxH0gYPbTggL6KUor+iXOdnjsvo93by0gl/OrUowPa/7N3C5UeHhZbs5kq/KwFv1NIjrP7lB8Tb84H5wCQYlYejK0SE02YYfK5B7X4XjlXOIhuyOYefal5+JCYBvM64fTh5+bIf77+viku1NB5sS4atmxOkf+NryQesdF3K3nMA8h+3pz0bmj1YuIqq9NhcSFT1C/WDtTa95dtH0Ys9REbiOTsQdjQDZkV+e/2F4q653Zar+HRE/OCcBRXr/RqvxXQDzEGuhZzRUbR2l0n2mEJGdEU7W8wpp/R+9Km28j1jMOMQndPLv6c3BrjgqF3NlDcz3ZkGclSR7xgqkCtDTunmG1aPtDa/eDycnjLbXVW1pJz2vEIiasdcyE8Zv9VuMpIjXglOWp0rbOmuGUUSVdYo5xelhcOkEFTyemzEAzVTbS9Dcc6j5qQKDEh+1wWcmyF8YDGf2kd3nDXLTjzrHlZmLyrc9/EX8Nam6r7eSqAjZY4bBs1j67DuPhh7nUkHjy3ORzAsnFk4bjT69aA85vzX49eDh5vv11vvz4Zk5N2pEstCX7sXgVF89AgMR2mPFT2QuLxc0N7RljvLuwhqetuRr+3UthoRLOdFJSMKvaF6Fy/uCvyQsHYFnYNguex6halzhuWd1KZoRQ3qvIgUh9iY/jO0apAuLvzj4RgMfRwwpY09HZ0gBOUMhc53up9wyOZ65WYS2eKXvkBEr4TlebtR9M5Gp4v63MpxxPuVjY5qIUEOQLjkgiZwKM8/lZRlMsrdF9njeDHK/+qDQUcTWfeBI09fnnVd40OX63+TrP9DQO4vTbZfTwDRAuGl7wWf4C3BRAipMd6bs5mCdC0Lq36CDc3m0b2A2LHIj7S6xko/JwvzWI5D1pQOP2pCBw2S1Wc2N8tTNS1BQBFV9bX+vr+FsrDEqezbpLp+PYmkVP/ygFpytD4Kr0yal3DSqtLOPyO9RF3swlI78LTxTqbRa6tw3wsQzaVNGd8U9//6qS922PPNCQMPUIJ4J+VnM2pz8+6UjgaCgKVkXs+Zk2a31BbXZZ1SEG9YMis1FdF1q4v/fd9MthAtlHMkwKM67MBjUH5XubuvoyX8jrAp2Ua+1cl1uxQc9l2ZYqNRe0rubPMgoK7YwJ7RP4ehtO4SD4oMOSORCDSmHVwn3tg=="
libeufin-
private fun getEbicsSubscriberDetailsInternal(subscriber: EbicsSubscriberEntity): EbicsClientSubscriberDetails {
    var bankAuthPubValue: RSAPublicKey? = null
    if (subscriber.bankAuthenticationPublicKey != null) {
        bankAuthPubValue = CryptoUtil.loadRsaPublicKey(
            subscriber.bankAuthenticationPublicKey?.bytes!!
        )
    }
    var bankEncPubValue: RSAPublicKey? = null
    if (subscriber.bankEncryptionPublicKey != null) {
        bankEncPubValue = CryptoUtil.loadRsaPublicKey(
            subscriber.bankEncryptionPublicKey?.bytes!!
        )
    }
    return EbicsClientSubscriberDetails(
        bankAuthPub = bankAuthPubValue,
        bankEncPub = bankEncPubValue,

        ebicsUrl = subscriber.ebicsURL,
        hostId = subscriber.hostID,
        userId = subscriber.userID,
        partnerId = subscriber.partnerID,

        customerSignPriv = CryptoUtil.loadRsaPrivateKey(subscriber.signaturePrivateKey.bytes),
        customerAuthPriv = CryptoUtil.loadRsaPrivateKey(subscriber.authenticationPrivateKey.bytes),
        customerEncPriv = CryptoUtil.loadRsaPrivateKey(subscriber.encryptionPrivateKey.bytes),
        ebicsIniState = subscriber.ebicsIniState,
        ebicsHiaState = subscriber.ebicsHiaState
    )
}


# apt update --allow-releaseinfo-change
apt-get install jq -y # qpdf xxd libxml2-utils openssl -y
client_pr_key="${CLIENT_PR_KEY:-/app/scripts/client_private_key.pem}"
client_pub_key="${CLIENT_PUB_KEY:-/app/scripts/client_public_key.pem}"

cat /app/scripts/backupfile | jq -r '.sigBlob' | openssl enc -d -base64 -A | openssl pkcs8 -inform DER -outform PEM -out $client_pr_key  -passin pass:$SECRET
openssl rsa -pubout -in $client_pr_key -out $client_pub_key
echo "client pk exported to $client_pr_key public key to $client_pub_key "

read -t 10 -p "Setup & startup of nexus and sandbox complete, starting Libeufin react-ui UI on localhost:3000, login with:  $LIBEUFIN_NEXUS_USERNAME $LIBEUFIN_NEXUS_PASSWORD "
#serve -s build
yarn start

