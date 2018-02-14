package munch.data.hour;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import munch.data.utils.PatternTexts;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 14/2/18
 * Time: 9:24 AM
 * Project: munch-data
 */
class HourExtractorTest {

    HourExtractor extractor = new HourExtractor();

    private boolean compare(String test, String pattern) {
        Set<OpenHour> testData = ImmutableSet.copyOf(extractor.extract(test));
        Set<OpenHour> patternData = extractPattern(pattern);
        return testData.equals(patternData);
    }

    private Set<OpenHour> extractPattern(String pattern) {
        return Arrays.stream(pattern.split(","))
                .flatMap(s -> extractor.extract(s).stream())
                .collect(Collectors.toSet());
    }

    @Test
    void testAssert() {
        Assertions.assertTrue(compare("Daily: 10am - 10pm",
                "Mon-Sun{10:00-22:00}"));

        Assertions.assertTrue(compare("Sun-Tue: 15:00 - 20:30",
                "Sun-Tue{15:00-20:30}"));

        Assertions.assertTrue(compare("Wed: 15:00 - 20:30, Tue: 15:00 - 20:30",
                "Tue-Wed{15:00-20:30}"));

        Assertions.assertTrue(compare("Sun-Tue: 15:00 - 20:30. Thu-Fri: 16:00 - 9pm",
                "Sun-Tue{15:00-20:30},Thu-Fri{16:00-21:00}"));

        Assertions.assertTrue(compare("Sun:15:15-20:30",
                "Sun{15:15-20:30}"));

        Assertions.assertTrue(compare("Sun, Eve of Ph: 10am - 10pm",
                "Sun{10:00-22:00},evePh{10:00-22:00}"));

        Assertions.assertFalse(compare("Daily: 10am - 10pm",
                "Sun{10:00-22:00}"));

    }

    @Test
    void single() {
        String text = "Monday to Sunday Lunch 11:30 am - 3 pm (Last Order at 2:30 pm); Dinner: 6 pm - 10 pm (Last Order at 9:30 pm)";
        List<OpenHour> extract = extractor.extract(text);
        PatternTexts patternTexts = extractor.parse(text);
        System.out.println(text);
        System.out.println(extract);
        System.out.println(patternTexts);
    }

    @Test
    void file() throws IOException {
        URL resource = Resources.getResource("hour-data.txt");
        List<String> lines = Resources.readLines(resource, Charset.forName("utf-8"));

        int failed = 0;
        int passed = 0;

        ListIterator<String> iterator = lines.listIterator();
        while (iterator.hasNext()) {
            String test = iterator.next();
            String pattern = iterator.next();

            if (compare(test, pattern)) {
                passed++;
            } else {
                failed++;
                System.out.println("Failed Test: " + test);
                Set<OpenHour> extracted = ImmutableSet.copyOf(extractor.extract(test));
                Set<OpenHour> expected = extractPattern(pattern);
                System.out.println("Failed Result:   " + extracted);
                System.out.println("Expected Result: " + expected);
                System.out.println();
            }
        }

        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
    }


}