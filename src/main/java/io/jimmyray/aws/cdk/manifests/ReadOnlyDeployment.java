package io.jimmyray.aws.cdk.manifests;

import io.jimmyray.aws.cdk.Constants;
import io.jimmyray.utils.Config;
import io.jimmyray.utils.Strings;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public final class ReadOnlyDeployment {

    public static Map<String, ? extends Object> manifest;

    // Get properties object
    private static final Properties properties = Config.properties;

    static {
        manifest = Map.of("apiVersion", "apps/v1",
                "kind", "Deployment",
                "metadata", Map.of("name", "read-only",
                        "namespace", "read-only",
                        "labels",
                        Map.of("owner", Strings.getPropertyString("labels.owner", properties,
                                Constants.NOT_FOUND.getValue()),
                                "env",
                                Strings.getPropertyString("labels.env", properties,
                                        Constants.NOT_FOUND.getValue()),
                                "app",
                                Strings.getPropertyString("labels.app", properties,
                                        Constants.NOT_FOUND.getValue()))),
                "spec", Map.of("revisionHistoryLimit", 3,
                        "selector", Map.of("matchLabels", Map.of("app", "read-only")),
                        "replicas", 3,
                        "strategy", Map.of("type", "RollingUpdate",
                                "rollingUpdate", Map.of("maxSurge", 10,
                                        "maxUnavailable", 1)),
                        "template", Map.of("metadata", Map.of("labels", Map.of("app", Strings.getPropertyString("labels.app", properties,
                                Constants.NOT_FOUND.getValue()),
                                "owner", Strings.getPropertyString("labels.owner", properties,
                                        Constants.NOT_FOUND.getValue()),
                                "env", Strings.getPropertyString("labels.env", properties,
                                        Constants.NOT_FOUND.getValue()))),
                                "spec", Map.of("securityContext", Map.of("fsGroup", 2000),
                                        "containers", List.of(Map.of("name", "read-only",
                                                "image", Strings.getPropertyString("deployment.image", properties,
                                                        Constants.DEPLOYMENT_IMAGE.getValue()),
                                                "imagePullPolicy", "IfNotPresent",
                                                "securityContext", Map.of("allowPrivilegeEscalation", false,
                                                        "runAsUser", 1000,
                                                        "readOnlyRootFilesystem", true),
                                                "resources", Map.of("limits", Map.of("cpu", "200m",
                                                        "memory", "20Mi"),
                                                        "requests", Map.of("cpu", "100m",
                                                                "memory", "10Mi")),
                                                "readinessProbe", Map.of("tcpSocket", Map.of("port", 8080),
                                                        "initialDelaySeconds", 5,
                                                        "periodSeconds", 10),
                                                "livenessProbe", Map.of("tcpSocket", Map.of("port", 8080),
                                                        "initialDelaySeconds", 15,
                                                        "periodSeconds", 20),
                                                "ports", List.of(Map.of("containerPort", 8080)),
                                                "volumeMounts", List.of(Map.of("mountPath", "/tmp",
                                                        "name", "tmp")))),
                                        "volumes", List.of(Map.of("name", "tmp", "emptyDir", Map.of()))))

                )
        );
    }
}
