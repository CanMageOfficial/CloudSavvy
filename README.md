<!-- TOC -->
  * [CLOUDSAVVY](#cloudsavvy)
  * [Running From Local Computer](#running-from-local-computer)
    * [Running From Command line From Local Computer](#running-from-command-line-from-local-computer)
    * [Running From Command line With Specific Region](#running-from-command-line-with-specific-region)
    * [Running From Command line With AWS Profile](#running-from-command-line-with-aws-profile)
  * [Deploying to AWS](#deploying-to-aws)
    * [Deploying to Personal AWS Account](#deploying-to-personal-aws-account)
  * [Sample Report](#sample-report)
  * [Development](#development)
    * [Setting Up Development Environment](#setting-up-development-environment)
    * [Debugging Arguments For Development](#debugging-arguments-for-development)
    * [Testing](#testing)
<!-- TOC -->

## CLOUDSAVVY
CloudSavvy monitors and detects anomalies in your AWS account, 
aiding in cost reduction and performance enhancement. 
It can be executed from your local computer or deployed to any region 
within your AWS account. CloudSavvy runs daily if deployed to AWS account, delivering results via 
email and storing them in an S3 bucket named `cloudsavvy-"AccountId"-"Region"`. 
The `template.yml` file is utilized to configure the necessary 
resources for running CloudSavvy.

## Running From Local Computer
### Running From Command line From Local Computer
./gradlew run   
Build system uses gradle.   
If no region is provided,
application detects all regions enabled in the AWS environment. Results are 
created at Results folder. IntelliJ IDEA Community edition is used for development.

### Running From Command line With Specific Region
./gradlew run --args='-r us-east-1'

### Running From Command line With AWS Profile
AWS_PROFILE=`Profile Name` ./gradlew run

## Deploying to AWS
### Deploying to Personal AWS Account
1. Install AWS CLI: https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html
2. Setup AWS CLI: https://docs.aws.amazon.com/cli/latest/userguide/getting-started-quickstart.html
3. Set region - optional
   `export AWS_REGION=us-east-1`
4. Run 1-create-bucket.sh script
   `bash 1-create-bucket.sh`
5. Run 2-deploy.sh script
   `bash 2-deploy.sh`

CloudSavvyExecutorFunction lambda function will be deployed to your default region
CloudSavvyExecutorFunction will be scheduled to run daily

## Sample Report

![alt text](https://raw.githubusercontent.com/CanMageOfficial/CloudSavvy/main/Samples/Sample_Report_1.png)

## Development

### Setting Up Development Environment
1. Install IntelliJ IDEA from https://www.jetbrains.com/idea/
2. Enable annotation processing https://www.baeldung.com/lombok-ide
3. Build project

### Debugging Arguments For Development
1. Running with a region:
   1. run --args="-r us-east-1"
2. Running with regions:
   1. run --args="-r us-east-1,us-east-2"
3. Environment variables to emulate lambda environment: 
   1. AWS_REGION=us-east-2;LAMBDA_TASK_ROOT=1;IGNORE_RESOURCE_AGE=true;RUNNING_AWS_ACCOUNT_ID=12345;
TO_EMAIL_ADDRESSES=`Email addresses to send results`,;IGNORE_AVAILABILITY=false;REGIONS=us-east-1,us-east-2,us-west-1;
RESULTS_BUCKET=`bucket to upload results`;
NOTIFICATION_TOPIC_ARN=`SNS topic arn to create notification when run completes`;
FROM_EMAIL_ADDRESS=`your email address to send email from`;IS_DEBUG=true

### Testing
1. Testing Redis:
   https://docs.aws.amazon.com/AmazonElastiCache/latest/red-ug/GettingStarted.ConnectToCacheNode.html

