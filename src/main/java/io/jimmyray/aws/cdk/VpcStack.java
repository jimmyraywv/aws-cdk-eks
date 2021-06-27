package io.jimmyray.aws.cdk;

import io.jimmyray.utils.Config;
import io.jimmyray.utils.Strings;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.core.Tags;
import software.amazon.awscdk.services.ec2.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class VpcStack extends Stack {
    private IVpc vpc;

    public VpcStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public VpcStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // Get properties object
        final Properties properties = Config.properties;

        Tags.of(scope).add("owner", properties.getProperty("labels.owner"));
        Tags.of(scope).add("env", properties.getProperty("labels.env"));
        Tags.of(scope).add("app", properties.getProperty("labels.app"));

        /*
        VPC Subnet Configs
         */
        List<SubnetConfiguration> subnets = new ArrayList<>();

        subnets.add(SubnetConfiguration.builder()
                .subnetType(SubnetType.PUBLIC)
                .name("public")
                .cidrMask(Strings.getPropertyInt("subnet.bits", properties, Constants.SUBNET_BITS.getIntValue()))
                .reserved(false)
                .build());

        subnets.add(SubnetConfiguration.builder()
                .subnetType(SubnetType.PRIVATE)
                .name("private")
                .cidrMask(Strings.getPropertyInt("subnet.bits", properties, Constants.SUBNET_BITS.getIntValue()))
                .reserved(false)
                .build());

        /*
         * VPC
         */
        this.vpc = Vpc.Builder.create(this, Strings.getPropertyString("vpc.id",
                properties,
                Constants.VPC_ID.getValue()))
                .cidr(Strings.getPropertyString("vpc.cidr",
                        properties,
                        Constants.VPC_CIDR.getValue()))
                .defaultInstanceTenancy(DefaultInstanceTenancy.DEFAULT)
                .enableDnsHostnames(true)
                .enableDnsSupport(true)
                .subnetConfiguration(subnets)
                .maxAzs(3)
                .natGateways(3)
                .natGatewayProvider(NatProvider.gateway())
                .natGatewaySubnets(SubnetSelection.builder().subnetType(SubnetType.PUBLIC).build())
                .build();
        Tags.of(vpc).add("network-type","cdk-test");
    }

    public IVpc getVpc() {
        return vpc;
    }


}
