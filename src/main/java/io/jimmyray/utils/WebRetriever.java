package io.jimmyray.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

/**
 * Provides helper methods to retrieve docs from URls
 */
public final class WebRetriever {

    public static void main(final String[] args) {
        String result = null;

        // Get properties object
        final Properties properties = Config.properties;

        try {
            result = WebRetriever.getRaw(Strings.getPropertyString("ssm.agent.installer.url", properties, ""));
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(result);
    }

    /**
     * Get raw file from web
     * @param in
     * @return
     * @throws IOException
     */
    public static String getRaw(final String in) throws IOException {
        URL url;
        String file = """
                """;
        url = new URL(in);
        URLConnection uc;
        uc = url.openConnection();
        uc.setRequestProperty("X-Requested-With", "Curl");

        BufferedReader reader = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String line = null;
        while ((line = reader.readLine()) != null)
            file = file + line + "\n";

        return file;
    }
}
