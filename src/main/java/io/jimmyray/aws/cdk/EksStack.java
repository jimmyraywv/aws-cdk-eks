package io.jimmyray.aws.cdk;

import io.jimmyray.aws.cdk.manifests.ReadOnlyDeployment;
import io.jimmyray.aws.cdk.manifests.ReadOnlyNamespace;
import io.jimmyray.aws.cdk.manifests.ReadOnlyService;
import io.jimmyray.aws.cdk.manifests.Yamls;
import io.jimmyray.utils.Config;
import io.jimmyray.utils.Strings;
import io.jimmyray.utils.YamlParser;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.eks.*;
import software.amazon.awscdk.services.iam.*;
import software.amazon.awscdk.services.kms.IKey;
import software.amazon.awscdk.services.kms.Key;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class EksStack extends Stack {
    public EksStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public EksStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // Get properties object
        final Properties properties = Config.properties;

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
        EKS Cluster and VPC
         */
        IKey secretsKey = Key.fromKeyArn(this, "EksSecretsKey", Strings.getPropertyString("eks.secrets.key.arn",
                properties,
                Constants.EKS_SECRETS_KEY.getValue()));

        // Gather policies for node role
        List<IManagedPolicy> policies = new ArrayList<>();
        policies.add(ManagedPolicy.fromManagedPolicyArn(this, "node-policy",
                Strings.getPropertyString("iam.policy.arn.eks.node", properties, Constants.NOT_FOUND.getValue())));
        policies.add(ManagedPolicy.fromManagedPolicyArn(this, "cni-policy",
                Strings.getPropertyString("iam.policy.arn.eks.cni", properties, Constants.NOT_FOUND.getValue())));
        policies.add(ManagedPolicy.fromManagedPolicyArn(this, "registry-policy",
                Strings.getPropertyString("iam.policy.arn.ecr.read", properties, Constants.NOT_FOUND.getValue())));
        policies.add(ManagedPolicy.fromManagedPolicyArn(this, "autoscaler-policy",
                Strings.getPropertyString("iam.policy.arn.eks.node.autoscaler", properties, Constants.NOT_FOUND.getValue())));
        policies.add(ManagedPolicy.fromManagedPolicyArn(this, "ssm-policy",
                Strings.getPropertyString("iam.policy.arn.ssm.core", properties, Constants.NOT_FOUND.getValue())));
        policies.add(ManagedPolicy.fromManagedPolicyArn(this, "kms-policy",
                Strings.getPropertyString("iam.policy.arn.kms.ssm.use", properties, Constants.NOT_FOUND.getValue())));

        String adminRole = Strings.getPropertyString("eks.admin.role",
                properties,
                Constants.EKS_ADMIN_ROLE.getValue());
        Role role = Role.Builder.create(this, adminRole)
                .roleName(adminRole)
                .assumedBy(new CompositePrincipal(new AccountRootPrincipal()))
                .managedPolicies(policies)
                .build();

        String eksId = Strings.getPropertyString("eks.id",
                properties,
                Constants.EKS_ID.getValue());

        Cluster cluster = Cluster.Builder.create(this, eksId)
                .clusterName(eksId)
                .defaultCapacity(Strings.getPropertyInt("eks.capacity", properties, Constants.EKS_DEFAULT_CAPACITY.getIntValue()))
                .defaultCapacityInstance(new InstanceType(Strings.getPropertyString("eks.instance.type",
                        properties,
                        Constants.EKS_INSTANCE_TYPE.getValue())))
                .defaultCapacityType(DefaultCapacityType.NODEGROUP)
                .endpointAccess(EndpointAccess.PUBLIC_AND_PRIVATE)
                .mastersRole(role)
                .version(KubernetesVersion.V1_19)
                .secretsEncryptionKey(secretsKey)
                .vpc(Vpc.Builder.create(this, Strings.getPropertyString("vpc.id",
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
                        .build())
                .build();

        /*
        Multiple k8s manifests, with dependencies, should be in the same KubernetesManifest object
         */
        /*KubernetesManifest.Builder.create(this, "read-only")
                .cluster(cluster)
                .manifest((List<? extends Map<String, ? extends Object>>) List.of(ReadOnlyNamespace.manifest,
                        ReadOnlyDeployment.manifest, ReadOnlyService.manifest))
                .overwrite(true)
                //.prune(true)
                .build();*/

        /*
        Multiple k8s manifests, with dependencies, should be in the same KubernetesManifest object
         */
        KubernetesManifest.Builder.create(this, "read-only")
                .cluster(cluster)
                .manifest((List<? extends Map<String, ? extends Object>>) List.of(YamlParser.parse(Yamls.namespace),
                        YamlParser.parse(Yamls.deployment), YamlParser.parse(Yamls.service)))
                .overwrite(true)
                //.prune(true)
                .build();

    }
}
