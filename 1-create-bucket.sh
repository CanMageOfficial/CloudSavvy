#!/bin/bash
set -eo pipefail

ENV_NAME=${1:-}
if [ -z "$ENV_NAME" ]; then
    read -p "Environment name (e.g. dev, prod, staging): " ENV_NAME
fi

ENV_FILE=".env.$ENV_NAME"

# Load existing values as defaults if env file already exists
BUCKET_NAME=""
AWS_PROFILE="default"
AWS_REGION="us-east-1"
if [ -f "$ENV_FILE" ]; then
    source "$ENV_FILE"
fi

read -p "AWS Profile Name [${AWS_PROFILE}]: " input
AWS_PROFILE=${input:-$AWS_PROFILE}

read -p "AWS Region [${AWS_REGION}]: " input
AWS_REGION=${input:-$AWS_REGION}

BUCKET_ID=$(dd if=/dev/random bs=8 count=1 2>/dev/null | od -An -tx1 | tr -d ' \t\n')
BUCKET_NAME=lambda-artifacts-$BUCKET_ID

aws s3 mb s3://$BUCKET_NAME --profile=$AWS_PROFILE --region=$AWS_REGION

# Write or update the env file
if [ -f "$ENV_FILE" ]; then
    sed -i '' "s|^BUCKET_NAME=.*|BUCKET_NAME=$BUCKET_NAME|" "$ENV_FILE"
    sed -i '' "s|^AWS_PROFILE=.*|AWS_PROFILE=$AWS_PROFILE|" "$ENV_FILE"
    sed -i '' "s|^AWS_REGION=.*|AWS_REGION=$AWS_REGION|" "$ENV_FILE"
else
    cat > "$ENV_FILE" <<EOF
BUCKET_NAME=$BUCKET_NAME
AWS_PROFILE=$AWS_PROFILE
AWS_REGION=$AWS_REGION
EMAIL_ADDRESS=
CREATE_SNS_TOPIC=false
EOF
    echo "Created $ENV_FILE — set EMAIL_ADDRESS before deploying."
fi

echo "Bucket $BUCKET_NAME saved to $ENV_FILE"
