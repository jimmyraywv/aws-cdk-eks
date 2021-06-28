package io.jimmyray.aws.cdk;

import io.jimmyray.aws.cdk.manifests.Yamls;
import io.jimmyray.utils.Config;
import io.jimmyray.utils.Strings;
import io.jimmyray.utils.WebRetriever;
import io.jimmyray.utils.YamlParser;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.core.Tags;
import software.amazon.awscdk.services.eks.Cluster;
import software.amazon.awscdk.services.eks.HelmChart;
import software.amazon.awscdk.services.eks.KubernetesManifest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static io.jimmyray.aws.cdk.helm.Values.readonlyValues;

public class K8sAppsStack extends Stack {
    public K8sAppsStack(final Construct scope, final String id) {
        this(scope, id, null, null);
    }

    /**
     * Entrypoint
     *
     * @param scope
     * @param id
     * @param props
     */
    public K8sAppsStack(final Construct scope, final String id, final StackProps props, Cluster cluster) {
        super(scope, id, props);

        // Get properties object
        final Properties properties = Config.properties;

        Tags.of(scope).add("owner", properties.getProperty("labels.owner"));
        Tags.of(scope).add("env", properties.getProperty("labels.env"));
        Tags.of(scope).add("app", properties.getProperty("labels.app"));

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
