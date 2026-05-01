#!/bin/bash
set -eo pipefail

ENV_NAME=${1:-}
if [ -z "$ENV_NAME" ]; then
    read -p "Environment name (e.g. dev, prod, staging): " ENV_NAME
fi

ENV_FILE=".env.$ENV_NAME"

if [ ! -f "$ENV_FILE" ]; then
    echo "Error: $ENV_FILE not found. Run 1-create-bucket.sh $ENV_NAME first."
    exit 1
fi

source "$ENV_FILE"

if [ -z "$BUCKET_NAME" ] || [ -z "$EMAIL_ADDRESS" ]; then
    echo "Error: BUCKET_NAME and EMAIL_ADDRESS must be set in $ENV_FILE"
    exit 1
fi

AWS_PROFILE=${AWS_PROFILE:-default}
AWS_REGION=${AWS_REGION:-us-east-1}
CREATE_SNS_TOPIC=${CREATE_SNS_TOPIC:-false}
TEMPLATE=template.yml

echo "Deploying to environment: $ENV_NAME"
echo "  Bucket:    $BUCKET_NAME"
echo "  Profile:   $AWS_PROFILE"
echo "  Region:    $AWS_REGION"
echo "  Email:     $EMAIL_ADDRESS"
echo "  SNS Topic: $CREATE_SNS_TOPIC"

./gradlew buildZip
aws cloudformation package --template-file $TEMPLATE --s3-bucket $BUCKET_NAME --output-template-file out.yml --profile=$AWS_PROFILE --region=$AWS_REGION
aws cloudformation deploy --template-file out.yml --stack-name cloudsavvy \
    --parameter-overrides EmailAddress=$EMAIL_ADDRESS LambdaBucket=$BUCKET_NAME CreateSnsTopic=$CREATE_SNS_TOPIC \
    --capabilities CAPABILITY_NAMED_IAM --profile=$AWS_PROFILE --region=$AWS_REGION
