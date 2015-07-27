package au.id.dkoussa.gocd_config_cleaner;

import nu.xom.Document;
import nu.xom.Serializer;

import java.io.FileOutputStream;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length < 1 || args.length > 2) {
            System.out.println("usage: <input> [server_id]");
            System.out.println("      input: path to the gocd file to sanitize");
            System.out.println("      server_id: is a GUID for the server, only needed");
            System.out.println("          if you plan on importing into a server");
            return;
        }

        String inputFile = args[0];
        String outputFile = inputFile + "___sanitized.xml";

        String serverId = "09e04df9-abf9-4c7a-bffd-f3d5782629cb";
        if (args.length == 2) {
            serverId = args[1];
        }

        RandomWords randomWords = new RandomWords();
        GocdConfigCleaner gocdConfigCleaner = new GocdConfigCleaner();
        gocdConfigCleaner.setRandomWords(randomWords);

        Document document = gocdConfigCleaner.clean(inputFile, serverId);
        print(document, outputFile);
    }

    private static void print(Document document, String outputFile) throws Exception {
        Serializer serializer = new Serializer(new FileOutputStream(outputFile), "ISO-8859-1");
        serializer.setIndent(2);
        serializer.setMaxLength(0);
        serializer.setPreserveBaseURI(false);
        serializer.write(document);
        serializer.flush();
    }
}
