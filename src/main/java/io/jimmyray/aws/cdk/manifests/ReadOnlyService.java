package io.jimmyray.aws.cdk.manifests;

import io.jimmyray.aws.cdk.Constants;
import io.jimmyray.utils.Config;
import io.jimmyray.utils.Strings;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ReadOnlyService {

    public static Map<String, ? extends Object> manifest;

    // Get properties object
    private static final Properties properties = Config.properties;

    static {
        manifest = Map.of("apiVersion", "v1",
                "kind", "Service",
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
                "spec", Map.of("ports", List.of(Map.of("port", 80,
                        "targetPort", 8080,
                        "protocol","TCP",
                        "name", "http")),
                        "type", "LoadBalancer",
                        "selector", Map.of("app","read-only")
                )
        );
    }
}

