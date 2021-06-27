package io.jimmyray.aws.cdk;

/**
 * Constants with helper methods
 */
public enum Constants {
    DEPLOYMENT_IMAGE("public.ecr.aws/r2l1x4g2/go-http-server:v0.1.0-23ffe0a715"),
    EKS_ADMIN_ROLE("EksClusterAdminRole"),
    EKS_DEFAULT_CAPACITY("0"),
    EKS_ID("cdk-eks"),
    EKS_INSTANCE_TYPE("m5.large"),
    EKS_SECRETS_KEY("<KMS_KEY_ARN>"),
    EKS_STACK("EksStack"),
    NOT_FOUND("not-found"),
    STACK_ACCOUNT("<ACCOUNT>"),
    STACK_REGION("us-east-2"),
    SUBNET_BITS("23"),
    VPC_CIDR("10.0.0.0/20"),
    VPC_ID("cdk-vpc"),
    VPC_STACK("VpcStack");

    private String value;

    Constants(String value) {
        this.value = value;
    }

    /**
     * String values
     * @return
     */
    public String getValue() {
        return value;
    }

    /**
     * Int values
     * @return
     */
    public int getIntValue() {
        return Integer.parseInt(value);
    }
}
