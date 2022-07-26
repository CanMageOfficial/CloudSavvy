package com.cloudSavvy.aws.common;

public enum AWSService {
    S3("S3"),
    DynamoDB("DynamoDB"),
    Lambda("Lambda"),
    VPC("VPC"),
    LOAD_BALANCER("Load Balancer"),
    Kinesis("Kinesis"),
    EC2("EC2"),
    RDS("RDS"),
    Amazon_Redshift("Amazon Redshift"),
    CloudWatch("CloudWatch"),
    ElastiCache("ElastiCache"),
    Amazon_EventBridge("Amazon EventBridge"),
    Amazon_SageMaker("Amazon SageMaker"),
    EFS("EFS"),
    AWS_Transfer("AWS Transfer Family"),
    FSx("FSx"),
    GLUE("AWS Glue"),
    AppStream_2("AppStream 2.0"),
    CloudFront("CloudFront"),
    EKS("Elastic Kubernetes Service"),
    Lightsail("Amazon Lightsail"),
    CloudSearch("CloudSearch"),
    ECS("Elastic Container Service"),
    SECRETS_MANAGER("Secrets Manager"),
    API_GATEWAY("Amazon API Gateway"),
    MEMORY_DB("Amazon MemoryDB for Redis");

    private final String text;

    AWSService(final String text) {
        this.text = text;
    }

    public String toString() {
        return text;
    }
}
