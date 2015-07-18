package au.id.dkoussa.gocd_config_cleaner;

import nu.xom.Document;
import nu.xom.Serializer;

import java.io.FileOutputStream;

public class Main {
    public static final String GOCD_CONFIG_FILE = "src/main/resources/gocd-config.xml";
    public static final String SANITIZED_CONFIG_FILE = "config-sanitized.xml";
    /**
     * Put the serverId of the go server that you want to import the sanitized
     * config into. If you don't, then the go server won't accept the new
     * config.
     */
    public static final String SERVER_ID = "09e04df9-abf9-4c7a-bffd-f3d5782629cb";

    public static void main(String[] args) throws Exception {
        RandomWords randomWords = new RandomWords();
        GocdConfigCleaner gocdConfigCleaner = new GocdConfigCleaner();
        gocdConfigCleaner.setRandomWords(randomWords);

        Document document = gocdConfigCleaner.clean(GOCD_CONFIG_FILE, SERVER_ID);
        print(document);
    }

    private static void print(Document document) throws Exception {
        Serializer serializer = new Serializer(new FileOutputStream(SANITIZED_CONFIG_FILE), "ISO-8859-1");
        serializer.setIndent(2);
        serializer.setMaxLength(0);
        serializer.setPreserveBaseURI(false);
        serializer.write(document);
        serializer.flush();
    }
}
