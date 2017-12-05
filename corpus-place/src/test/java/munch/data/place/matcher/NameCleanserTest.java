package munch.data.place.matcher;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by: Fuxing
 * Date: 6/12/2017
 * Time: 3:28 AM
 * Project: munch-data
 */
class NameCleanserTest {

    @Test
    void locationTrimming() throws Exception {
        PatternSplit pattern = NameCleanser.LocationCleanser.PATTERN_LOCATION_JOINER;

        assertEquals(pattern.matcher("food at ").replaceAll(""), "food");
        assertEquals(pattern.matcher("food - ").replaceAll(""), "food");
        assertEquals(pattern.matcher("food – ").replaceAll(""), "food");
        assertEquals(pattern.matcher("food — ").replaceAll(""), "food");
        assertEquals(pattern.matcher("food @ ").replaceAll(""), "food");
        assertEquals(pattern.matcher("food @").replaceAll(""), "food");
        assertEquals(pattern.matcher("food at place ").replaceAll(""), "food at place ");
    }

    @Test
    void multiWhitespace() throws Exception {
        assertEquals(NameCleanser.fixWhitespace("food  @"), "food @");
        assertEquals(NameCleanser.fixWhitespace("food  @ bishan "), "food @ bishan ");
    }

    @Test
    void pteltd() throws Exception {
        Pattern pattern = NameCleanser.PATTERN_COMPANY;

        assertEquals(pattern.matcher("Ser Seng Herbs Restaurant").replaceAll(""), "Ser Seng Herbs Restaurant");
        assertEquals(pattern.matcher("Islamic Restaurant pte ltd").replaceAll(""), "Islamic Restaurant ");
        assertEquals(pattern.matcher("Islamic Restaurant pte. ltd.").replaceAll(""), "Islamic Restaurant ");
        assertEquals(pattern.matcher("Islamic Restaurant pte. ltd").replaceAll(""), "Islamic Restaurant ");
        assertEquals(pattern.matcher("Islamic Restaurant pte. ltd Singapore").replaceAll(""), "Islamic Restaurant  Singapore");
    }
}