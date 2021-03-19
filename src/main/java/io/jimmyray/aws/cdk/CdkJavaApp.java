package io.jimmyray.aws.cdk;

import software.amazon.awscdk.core.App;
import software.amazon.awscdk.core.Environment;
import software.amazon.awscdk.core.StackProps;

public class CdkJavaApp {
    public static void main(final String[] args) {
        App app = new App();

        StackProps props = StackProps.builder().env(Environment.builder()
                .account(Constants.STACK_ACCOUNT.getValue())
                .region(Constants.STACK_REGION.getValue()).build())
                .build();

        new EksStack(app, Constants.EKS_STACK.getValue(), props);
        app.synth();
    }
}
