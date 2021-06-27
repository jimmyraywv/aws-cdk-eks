package io.jimmyray.aws.cdk;

import io.jimmyray.aws.cdk.manifests.Yamls;
import io.jimmyray.utils.Config;
import io.jimmyray.utils.Strings;
import io.jimmyray.utils.WebRetriever;
import io.jimmyray.utils.YamlParser;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.core.*;
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

import static io.jimmyray.aws.cdk.helm.Values.readonlyValues;

/**
 * CDK EKS Stack
 */
public class EksStack extends Stack {
    public EksStack(final Construct scope, final String id) {
        this(scope, id, null, null);
    }

    /**
     * Entrypoint
     *
     * @param scope
     * @param id
     * @param props
     */
    public EksStack(final Construct scope, final String id, final StackProps props, IVpc vpc) {
        super(scope, id, props);

        // Get properties object
        final Properties properties = Config.properties;

        Tags.of(scope).add("owner", properties.getProperty("labels.owner"));
        Tags.of(scope).add("env", properties.getProperty("labels.env"));
        Tags.of(scope).add("app", properties.getProperty("labels.app"));

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

        Tags.of(cluster).add("owner", properties.getProperty("labels.owner"));
        Tags.of(cluster).add("env", properties.getProperty("labels.env"));
        Tags.of(cluster).add("app", properties.getProperty("labels.app"));

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
        Nodegroup nodegroup = Nodegroup.Builder.create(this, "ng1")
                .cluster(cluster)
                //.launchTemplateSpec(LaunchTemplateSpec.builder().id("cdk-eks-launch-template").build())
                .tags(Map.of("owner","jimmy","env","dev","app","read-only"))
                .amiType(NodegroupAmiType.AL2_X86_64)
                .capacityType(CapacityType.ON_DEMAND)
                .desiredSize(3)
                .maxSize(5)
                .minSize(3)
                .diskSize(100)
                .labels(Map.of("node-group", "ng1", "instance-type", Strings.getPropertyString("eks.instance.type",
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

        Tags.of(nodegroup).add("name", "ng1-node");
        Tags.of(nodegroup).add("owner", properties.getProperty("labels.owner"));
        Tags.of(nodegroup).add("env", properties.getProperty("labels.env"));
        Tags.of(nodegroup).add("app", properties.getProperty("labels.app"));

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
                        YamlParser.parse(Yamls.deployment),
                        YamlParser.parse(Yamls.service.replace("<REMOTE_ACCESS_CIDRS>",
                                Strings.getPropertyString("remote.access.cidrs", properties, "")))))
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

        HelmChart.Builder.create(this, "readonly")
                .cluster(cluster)
                .chart(Strings.getPropertyString("helm.chart.name", properties, ""))
                .version(Strings.getPropertyString("helm.chart.version", properties, ""))
                .repository(Strings.getPropertyString("helm.repo.url", properties, ""))
                .values(YamlParser.parse(readonlyValues))
                .createNamespace(false)
                .build();
    }
}
