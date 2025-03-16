#!/bin/bash
set -eo pipefail
ARTIFACT_BUCKET=$(cat bucket-name.txt)
TEMPLATE=template.yml

read -p 'Email Address To Receive Daily Report: ' emailAddress
read -p "AWS Profile Name, leave empty for default value: " awsProfile
awsProfile=${awsProfile:-default}

./gradlew buildZip
aws cloudformation package --template-file $TEMPLATE --s3-bucket $ARTIFACT_BUCKET --output-template-file out.yml --profile=$awsProfile
aws cloudformation deploy --template-file out.yml --stack-name cloudsavvy \
    --parameter-overrides EmailAddress=$emailAddress LambdaBucket=$ARTIFACT_BUCKET \
    --capabilities CAPABILITY_NAMED_IAM --profile=$awsProfile