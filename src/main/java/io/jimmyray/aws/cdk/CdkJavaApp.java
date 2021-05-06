package io.jimmyray.aws.cdk;

import io.jimmyray.utils.Config;
import io.jimmyray.utils.Strings;
import software.amazon.awscdk.core.App;
import software.amazon.awscdk.core.Environment;
import software.amazon.awscdk.core.StackProps;

import java.util.Properties;

public class CdkJavaApp {
    public static void main(final String[] args) {
        App app = new App();

        // create properties object
        Properties properties = Config.properties;

        StackProps props = StackProps.builder().env(Environment.builder()
                .account(Strings.getPropertyString("stack.account",
                        properties,
                        Constants.STACK_ACCOUNT.getValue()))
                .region(Strings.getPropertyString("stack.region",
                        properties,
                        Constants.STACK_REGION.getValue())).build())
                .build();

        new EksStack(app, Strings.getPropertyString("eks.stack",
                properties,
                Constants.EKS_STACK.getValue()), props);
        app.synth();
    }
}
