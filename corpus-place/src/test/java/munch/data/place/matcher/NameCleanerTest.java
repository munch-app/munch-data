package munch.data.place.matcher;

import munch.data.utils.PatternSplit;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by: Fuxing
 * Date: 6/12/2017
 * Time: 3:28 AM
 * Project: munch-data
 */
class NameCleanerTest {

    @Test
    void locationTrimming() throws Exception {
        PatternSplit pattern = LocationCleaner.PATTERN_LOCATION_JOINER;

        assertEquals(pattern.matcher("food at ").replaceAll(""), "food");
        assertEquals(pattern.matcher("food - ").replaceAll(""), "food");
        assertEquals(pattern.matcher("food – ").replaceAll(""), "food");
        assertEquals(pattern.matcher("food — ").replaceAll(""), "food");
        assertEquals(pattern.matcher("food @ ").replaceAll(""), "food");
        assertEquals(pattern.matcher("food @").replaceAll(""), "food @");
    }

    @Test
    void multiWhitespace() throws Exception {
        assertEquals(NameNormalizer.trim("food  @"), "food @");
        assertEquals(NameNormalizer.trim("food  @ bishan "), "food @ bishan");
    }

    @Test
    void companyExtension() throws Exception {
        Pattern pattern = NameCleaner.PATTERN_COMPANY;

        assertEquals(pattern.matcher("Ser Seng Herbs Restaurant").replaceAll(""), "Ser Seng Herbs Restaurant");
        assertEquals(pattern.matcher("Islamic Restaurant pte ltd").replaceAll(""), "Islamic Restaurant ");
        assertEquals(pattern.matcher("Islamic Restaurant pte. ltd.").replaceAll(""), "Islamic Restaurant ");
        assertEquals(pattern.matcher("Islamic Restaurant pte. ltd").replaceAll(""), "Islamic Restaurant ");
        assertEquals(pattern.matcher("Islamic Restaurant pte. ltd Singapore").replaceAll(""), "Islamic Restaurant  Singapore");
    }

    @Test
    void locationCleaner() throws Exception {
        LocationCleaner cleaner = new LocationCleaner();
        assertEquals(cleaner.clean("PastaJ @ Bishan"), "PastaJ");
    }
}