package au.id.dkoussa.gocd_config_cleaner;

import nu.xom.Document;
import nu.xom.Serializer;

import java.io.FileOutputStream;

public class Main {
    public static void main(String[] args) throws Exception {
        RandomWords randomWords = new RandomWords();
        GocdConfigCleaner gocdConfigCleaner = new GocdConfigCleaner();
        gocdConfigCleaner.setRandomWords(randomWords);

        Document document = gocdConfigCleaner.clean();
        print(document);
    }

    private static void print(Document document) throws Exception {
        Serializer serializer = new Serializer(new FileOutputStream("config-sanitized.xml"), "ISO-8859-1");
        serializer.setIndent(2);
        serializer.setMaxLength(0);
        serializer.setPreserveBaseURI(false);
        serializer.write(document);
        serializer.flush();
    }
}
