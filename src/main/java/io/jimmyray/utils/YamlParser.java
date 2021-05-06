package io.jimmyray.utils;

import io.jimmyray.aws.cdk.manifests.Yamls;
import org.yaml.snakeyaml.Yaml;
import java.util.Map;

public class YamlParser {
    public static void main(final String[] args) {
        Map<String, Object> out = YamlParser.parse(Yamls.namespace);
        System.out.println(out);

        out = YamlParser.parse(Yamls.deployment);
        System.out.println(out);

        out = YamlParser.parse(Yamls.service);
        System.out.println(out);
    }

    public static Map<String, Object> parse(final String in) {
        Yaml yaml = new Yaml();
        return yaml.load(in);
    }
}
