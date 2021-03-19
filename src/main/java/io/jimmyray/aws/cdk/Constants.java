package io.jimmyray.aws.cdk;

public enum Constants {
    VPC_CIDR("10.0.0.0/20"),
    VPC_ID("cdk-vpc"),
    EKS_ID("cdk-eks"),
    EKS_INSTANCE_TYPE("m5.large"),
    EKS_ADMIN_ROLE("EksClusterAdminRole"),
    STACK_ACCOUNT("<ACCOUNT>"),
    STACK_REGION("us-east-1"),
    VPC_STACK("VpcStack"),
    EKS_STACK("EksStack");

    private String value;

    Constants(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
