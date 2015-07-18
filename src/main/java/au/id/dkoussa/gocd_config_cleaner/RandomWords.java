package au.id.dkoussa.gocd_config_cleaner;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

class RandomWords {

    private final List<String> strings;

    public RandomWords() throws Exception {
        List<String> words = Files.readAllLines(Paths.get("/usr/share/dict/words"));
        strings = new LinkedList<String>();
        for (String word : words) {
            if (word.matches("[a-zA-Z]{4,}")) {
                strings.add(word.toLowerCase());
            }
        }
        Collections.shuffle(strings);
    }

    public String next() {
        return strings.remove(0) + "-" + strings.remove(0);
    }
}
