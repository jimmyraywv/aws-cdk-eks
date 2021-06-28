package io.jimmyray.aws.cdk;

import io.jimmyray.utils.Config;
import io.jimmyray.utils.Strings;
import software.amazon.awscdk.core.App;
import software.amazon.awscdk.core.Environment;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.eks.Cluster;

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

        VpcStack vpcStack = new VpcStack(app, Strings.getPropertyString("vpc.stack",
                properties,
                Constants.VPC_STACK.getValue()), props);

        IVpc vpc = vpcStack.getVpc();

        EksStack eksStack = new EksStack(app, Strings.getPropertyString("eks.stack",
                properties,
                Constants.EKS_STACK.getValue()), props, vpc);

        Cluster cluster = eksStack.getCluster();

        new K8sAppsStack(app, Strings.getPropertyString("k8s.apps.stack",
                properties,
                Constants.K8S_APPS_STACK.getValue()), props, cluster);

        app.synth();
    }
}
