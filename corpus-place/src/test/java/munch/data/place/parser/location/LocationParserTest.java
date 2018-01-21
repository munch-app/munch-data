package munch.data.place.parser.location;

import org.junit.jupiter.api.Test;

/**
 * Created by: Fuxing
 * Date: 21/1/2018
 * Time: 5:46 PM
 * Project: munch-data
 */
class LocationParserTest {

    @Test
    void formatAddress() throws Exception {
        System.out.println(LocationParser.formatAddress("bishan street 13"));
        System.out.println(LocationParser.formatAddress("bishan street 13 (market and hawker)"));
        System.out.println(LocationParser.formatAddress("bishan street 13 (market and hawker) #05-4b"));
        System.out.println(LocationParser.formatAddress("bishan street 13 (market and hawker) #05-4ba"));
    }
}