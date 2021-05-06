package io.jimmyray.aws.cdk.manifests;

import io.jimmyray.aws.cdk.Constants;
import io.jimmyray.utils.Config;
import io.jimmyray.utils.Strings;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public final class ReadOnlyNamespace {

    public static Map<String, ? extends Object> manifest;

    // Get properties object
    private static final Properties properties = Config.properties;

    static {
        manifest = Map.of("apiVersion", "v1",
                "kind", "Namespace",
                "metadata", Map.of("name", "read-only", "labels",
                        Map.of("owner", Strings.getPropertyString("labels.owner", properties,
                                Constants.NOT_FOUND.getValue()),
                                "env",
                                Strings.getPropertyString("labels.env", properties,
                                        Constants.NOT_FOUND.getValue()),
                                "app",
                                Strings.getPropertyString("labels.app", properties,
                                        Constants.NOT_FOUND.getValue())))
        );
    }
}
