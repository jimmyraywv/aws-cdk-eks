package io.jimmyray.aws.cdk;

import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.eks.*;
import software.amazon.awscdk.services.iam.AccountRootPrincipal;
import software.amazon.awscdk.services.iam.CompositePrincipal;
import software.amazon.awscdk.services.iam.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class EksStack extends Stack {
    public EksStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public EksStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        /*
        VPC Subnet Configs
         */
        List<SubnetConfiguration> subnets = new ArrayList<>();

        subnets.add(SubnetConfiguration.builder()
                .subnetType(SubnetType.PUBLIC)
                .name("public")
                .cidrMask(23)
                .reserved(false)
                .build());

        subnets.add(SubnetConfiguration.builder()
                .subnetType(SubnetType.PRIVATE)
                .name("private")
                .cidrMask(23)
                .reserved(false)
                .build());

        /*
        EKS Cluster and VPC
         */
        Role role = Role.Builder.create(this, Constants.EKS_ADMIN_ROLE.getValue())
                .roleName(Constants.EKS_ADMIN_ROLE.getValue())
                .assumedBy(new CompositePrincipal(new AccountRootPrincipal()))
                .build();

        /*@NotNull IVpc vpc = Vpc.fromLookup(this, "vpcLookup", VpcLookupOptions.builder()
                .vpcName(Constants.VPC_STACK + "/" + Constants.VPC_ID).build());*/

        Cluster cluster = Cluster.Builder.create(this, Constants.EKS_ID.getValue())
                .clusterName(Constants.EKS_ID.getValue())
                .defaultCapacity(3)
                .defaultCapacityInstance(new InstanceType(Constants.EKS_INSTANCE_TYPE.getValue()))
                .defaultCapacityType(DefaultCapacityType.NODEGROUP)
                .endpointAccess(EndpointAccess.PUBLIC_AND_PRIVATE)
                .mastersRole(role)
                .version(KubernetesVersion.V1_19)
                .vpc(Vpc.Builder.create(this, Constants.VPC_ID.getValue())
                        .cidr(Constants.VPC_CIDR.getValue())
                        .defaultInstanceTenancy(DefaultInstanceTenancy.DEFAULT)
                        .enableDnsHostnames(true)
                        .enableDnsSupport(true)
                        .subnetConfiguration(subnets)
                        .maxAzs(3)
                        .natGateways(3)
                        .natGatewayProvider(NatProvider.gateway())
                        .natGatewaySubnets(SubnetSelection.builder().subnetType(SubnetType.PUBLIC).build())
                        .build())
                .build();

        KubernetesManifest.Builder.create(this, "read-only")
                .cluster(cluster)
                .manifest(asList(
                        Map.of("apiVersion", "v1",
                                "kind", "Namespace",
                                "metadata", Map.of("name", "read-only")
                        )
                ))
                .overwrite(true)
                //.prune(true)
                .build();
    }
}
