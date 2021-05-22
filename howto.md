## Setup

- The docker image contains jar file and dependencies of [java-ebics-client] (https://github.com/element36-io/ebics-java-client).  
- First step is to contact your bank to request EBICS access and sign the needed documents.
- They might ask for an IP address and only allow requests from this address.
- Find your banks EBICS parameters, eg [EBICS parameters for Zürcher Kantonalbank ](https://www.zkb.ch/media/dok/efinance/ebics-verbindungsparameter.pdf) (in German) 
- In addition your bank will provide you with more parameters such as user id.
- Put those parameters in the `$HOME/ebics/client/ebics.txt` properties file.


## Usage

Start command line tool in the container: 

	docker run ebics java -cp "./ebics-cli-1.2.jar:lib/*" org.kopi.ebics.client.EbicsClient --help
	
Create 	`$HOME/ebics` and `$HOME/ebics/out` to store parameter (ebics.txt) files, keys and output to a local directory. 

Once you configured `ebics.txt` you can create the user and send the `INI` and `HIA` request.

	docker run -v $HOME/ebics:/root/ebics ebics -cp "ebics-cli-1.2.jar:lib/*" --create

    docker run -v $HOME/ebics:/root/ebics ebics -cp "ebics-cli-1.2.jar:lib/*" --ini --hia"

Then you need to print the generated initialization letters, they are generated in text format. Afer printing, send those letters to your bank - they need that to create the Ebics login. The letters are in `./client/users/<userId>/letters/`


After this you can send the `HPB` request to fetch the bank parameters.

	docker run -v $HOME/ebics:/root/ebics ebics -cp "ebics-cli-1.2.jar:lib/*" --hpb


Then you need to compare the if the bank encryption keys from the `HPB` request match with the keys you see in your documents.

Now the client is ready to use, you can fetch the account data with a `STA` request

	docker run -v $HOME/ebics:/root/ebics ebics -cp "ebics-cli-1.2.jar:lib/*" --sta -o /root/ebics/out/sta-test.txt
	
The successful setup is shown by:  

	2021-05-22 07:53:48,251  INFO - Configuration initialization
	2021-05-22 07:53:48,254  INFO - Loading user 4444444
	2021-05-22 07:53:48,355  INFO - The user 4444444 was loaded successfully
	
	
## Typical traps

### ERROR - Cannot download the requested file

Daily statements can only be retrieved once - then you get an error if you request again. 
This Ebics "error" sticks, until you have a new transaction on your accounts. 

