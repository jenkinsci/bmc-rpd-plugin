# BMC RPD Plugin
The RPD Plugin integrates Jenkins with BMC Release Package and Deployment (RPD) by adding two build and post-build actions to your Jenkins build configuration, which can be executed separately or combined sequentially

## Features
*	Create an instance
*	Deploy an instance

## Requirements
*	RPD 4.4.00 and later
*	Jenkins 1.639 and later

## Build

### Dependencies
* [Apache Maven][maven] 3.0.5 or later

In order to build this plugin from source you need to run a following command in project root directory:
```shell
	mvn package
```
This command will build **.hpi** file for you.
### Install locally
1.  Run **mvn package** to build `/target/bmc-rpd.hpi`
2.  Remove previous **bmc-rpd.hpi** version in `$user.home/.jenkins/plugins/`
3.  Copy new `/target/bmc-rpd.hpi` to `$user.home/.jenkins/plugins/`
4.  Restart Jenkins

## RPD Plugin Configuration

### To configure the RPD Plugin:
1.	Go to Manage Jenkins > Configure System.
2.	In the BMC Release Package and Deployment Configuration section, click Add, and provide the following information:
	*	Set as Default Profile: Default profile. If you do not select the default profile, the first one will be selected by default. 
	*	Server Profile Name: Custom name of the RPD server.
	*	RPD Server URL: RPD server URL.
	*	RPD User Token: User token that will be used to authenticate against RPD. For more information, see https://docs.bmc.com/docs/display/rpd48/Creating+authentication+tokens.
3.	Click Save

## Job Configuration
### To create RPD instance:
1.	Create a new job by clicking New Item.
2.	In the Build section, from the Add build step list, select RPD Create Instance.
3.	In the RPD Create Instance section, provide the following information:
	*	Server Profile Name: Custom name of the RPD server.
	*	Package Name: Name of the package where you want to create the RPD instance.
	*	Instance Name: (Optional) Custom name for the RPD instance that you want to create.
4.	Click Save.

### To deploy RPD instance:
1.	Create a new job by clicking New Item.
2.	In the Build section, from the Add build step list, select RPD Deploy Instance.
3.	In the RPD Create Instance section, provide the following information:
	*	Server Profile Name: Custom name of the RPD server.
	*	Package Name: Name of the package containing the RPD instance that you want to deploy. 
	*	Instance Name: (Optional) Custom name of the RPD instance that you want to deploy.
	*	Route Name: Route where to deploy the RPD instance.
	*	Environment Name: Environment where to deploy the RPD instance.
4.	Click Save.

## Job Configuration as a Post-build Action
### To create RPD instance:
1.	Create a new job by clicking New Item.
2.	In the Post-build Actions section, from the Add post-build action list, select RPD Create Instance.
3.	In the RPD Create Instance section, provide the following information:
	*	Server Profile Name: Custom name of the RPD server.
	*	Package Name: Name of the package where you want to create the RPD instance.
	*	Instance Name: (Optional) Custom name for the RPD instance that you want to create.
4.	Click Save.

### To deploy RPD instance:
1.	Create a new job by clicking New Item.
2.	In the Post-build Actions section, from the Add post-build action list, select RPD Deploy Instance.
3.	In the RPD Create Instance section, provide the following information:
	*	Server Profile Name: Custom name of the RPD server.
	*	Package Name: Name of the package containing the RPD instance that you want to deploy. 
	*	Instance Name: (Optional) Custom name of the RPD instance that you want to deploy.
	*	Route Name: Route where to deploy the RPD instance.
	*	Environment Name: Environment where to deploy the RPD instance.
4.	Click Save.

## Variables
During instance creation, use the following custom variables:
*	${RPD_<Package name>_instance_name} – allows using an instance name for the next deployment or in some kinds of notifications.
*	${RPD_<Package name>_instance_id} – allows using an instance name for the next deployment or in some kinds of notifications.
You can also use standard Jenkins variables. To see the list of standard variables, go to [Jenkins Environment Variables] .


## RPD Secure Connection
### To install the certificate authority (CA):
1.	Go to the necessary URL, click the HTTPS certificate chain, and then click Certificate information.
2.	On the Details tab, click Copy to File…
3.	Install the CA by running the following command: keytool -import -alias example -keystore/path/to/cacerts -file example.der.


[maven]: https://maven.apache.org/
[Jenkins Environment Variables]: https://wiki.jenkins-ci.org/display/JENKINS/Building+a+software+project#Buildingasoftwareproject-JenkinsSetEnvironmentVariables.
