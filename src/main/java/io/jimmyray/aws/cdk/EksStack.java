package io.jimmyray.aws.cdk;

import io.jimmyray.aws.cdk.manifests.Yamls;
import io.jimmyray.utils.Config;
import io.jimmyray.utils.Strings;
import io.jimmyray.utils.WebRetriever;
import io.jimmyray.utils.YamlParser;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.eks.*;
import software.amazon.awscdk.services.iam.*;
import software.amazon.awscdk.services.kms.IKey;
import software.amazon.awscdk.services.kms.Key;

import java.io.IOException;
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
         * VPC
         */
        IVpc vpc = Vpc.Builder.create(this, Strings.getPropertyString("vpc.id",
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

        /*
        EKS Cluster
         */
        IKey secretsKey = Key.fromKeyArn(this, "EksSecretsKey", Strings.getPropertyString("eks.secrets.key.arn",
                properties,
                Constants.EKS_SECRETS_KEY.getValue()));

        /*
         * Use existing master admin role
         */
        @NotNull IRole admin = Role.fromRoleArn(this, "admin", Strings.getPropertyString("iam.account.admin.role.arn",
                properties, ""));

        String eksId = Strings.getPropertyString("eks.id",
                properties,
                Constants.EKS_ID.getValue());

        Cluster cluster = Cluster.Builder.create(this, eksId)
                .clusterName(eksId)
                .defaultCapacity(Strings.getPropertyInt("eks.default.capacity", properties, Constants.EKS_DEFAULT_CAPACITY.getIntValue()))
                .endpointAccess(EndpointAccess.PUBLIC_AND_PRIVATE)
                .mastersRole(admin)
                .version(KubernetesVersion.V1_19)
                .secretsEncryptionKey(secretsKey)
                .vpc(vpc)
                .build();

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

        Role nodeRole = Role.Builder.create(this, "eks-nodes-role")
                .roleName("EksNodes")
                .managedPolicies(policies)
                .assumedBy(new ServicePrincipal(Strings.getPropertyString("ec2.service.name", properties, "")))
                .build();

        /*
         * Build Nodegroup
         */
        Nodegroup.Builder.create(this, "ng1")
                .cluster(cluster)
                .amiType(NodegroupAmiType.AL2_X86_64)
                .capacityType(CapacityType.ON_DEMAND)
                .desiredSize(3)
                .maxSize(5)
                .minSize(3)
                .diskSize(100)
                .labels(Map.of("node-group","ng1","instance-type",Strings.getPropertyString("eks.instance.type",
                        properties,
                        Constants.EKS_INSTANCE_TYPE.getValue())))
                .remoteAccess(NodegroupRemoteAccess.builder()
                        .sshKeyName(Strings.getPropertyString("ssh.key.name",
                        properties, "")).build())
                .nodegroupName("ng1")
                .instanceTypes(List.of(new InstanceType(Strings.getPropertyString("eks.instance.type",
                        properties,
                        Constants.EKS_INSTANCE_TYPE.getValue()))))
                .subnets(SubnetSelection.builder().subnets(cluster.getVpc().getPrivateSubnets()).build())
                .nodeRole(nodeRole)
                .build();

        /*
        Multiple k8s manifests, with dependencies, should be in the same KubernetesManifest object
         */
        /*KubernetesManifest.Builder.create(this, "read-only")
                .cluster(cluster)
                .manifest((List<? extends Map<String, ? extends Object>>) List.of(ReadOnlyNamespace.manifest,
                        ReadOnlyDeployment.manifest, ReadOnlyService.manifest))
                .overwrite(true)
                .build();*/

        /*
        Multiple k8s manifests, with dependencies, should be in the same KubernetesManifest object
         */
        KubernetesManifest.Builder.create(this, "read-only")
                .cluster(cluster)
                .manifest(List.of(YamlParser.parse(Yamls.namespace),
                        YamlParser.parse(Yamls.deployment), YamlParser.parse(Yamls.service)))
                .overwrite(true)
                .build();

        /*
         * Parse multiple docs in same string
         */
        String yamlFile = null;

        /*
         * Try to get the YAML from GitHub
         */
        try {
            yamlFile = WebRetriever.getRaw(Strings.getPropertyString("ssm.agent.installer.url", properties, ""));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (yamlFile == null) yamlFile = Yamls.ssmAgent;

        if (null != yamlFile && !yamlFile.isBlank()) {

            Iterable<Object> manifestYamls = YamlParser.parseMulti(yamlFile);
            List manifestList = new ArrayList();
            for (Object doc : manifestYamls) {
                manifestList.add((Map<String, ? extends Object>) doc);
            }

            KubernetesManifest.Builder.create(this, "ssm-agent")
                    .cluster(cluster)
                    .manifest(manifestList)
                    .overwrite(true)
                    .build();
        }
    }
}
