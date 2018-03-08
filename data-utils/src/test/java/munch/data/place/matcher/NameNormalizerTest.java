package munch.data.place.matcher;

import org.junit.jupiter.api.Test;

/**
 * Created by: Fuxing
 * Date: 5/2/2018
 * Time: 3:10 PM
 * Project: munch-data
 */
class NameNormalizerTest {

    NameNormalizer nameNormalizer = new NameNormalizer();

    @Test
    void name() throws Exception {
        System.out.println(nameNormalizer.normalize("Place &amp; at is good"));
        System.out.println(nameNormalizer.normalize("Place &amp;Amp; at is good"));
        System.out.println(nameNormalizer.normalize("Place &amp;Amp;Amp; at is good"));

        System.out.println(nameNormalizer.normalize("fiancé, ” “protégé,” and “cliché"));

        System.out.println(nameNormalizer.normalize("á|â|à|é|ê|è|ë|ē|î|ï|ó|ô|ò|û|ù|ü|ÿ|ç"));
    }
}