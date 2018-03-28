package munch.data.location;

import com.google.common.base.Joiner;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 28/3/18
 * Time: 1:57 PM
 * Project: munch-data
 */
class LocationParserTest {

    AddressGrouping grouping;
    SingaporeCityParser cityParser;

    @BeforeEach
    void setUp() {
        Injector injector = Guice.createInjector();
        grouping = injector.getInstance(AddressGrouping.class);
        cityParser = injector.getInstance(SingaporeCityParser.class);
    }

    void print(String address) {
        System.out.println(address);

        Set<List<String>> group = grouping.group(address);
        for (List<String> tokens : group) {
            System.out.println(Joiner.on("").join(tokens));
            LocationData data = cityParser.parse(tokens);
            if (data != null) {
                System.out.println(data);
            }
        }
        System.out.println();
    }

    @Test
    void address() {
        print("96 Somerset Road, #01-01 Pan Pacific Serviced Suites Orchard (Next To 313@Somerset), Singapore 238163");
        print("50 Siloso Beach Walk, #01-03 Sentosa, Singapore 099000");
        print("120 Cantonment Road, #01-02 Maritime House, Singapore 089760");
        print("30 Merchant Road #01-03 Riverside Point, Singapore 058282");
        print("3A River Valley Road, #01-05 Merchants' Court, Singapore 179020");
        print("Block 1206 East Coast Parkway #01-07/08,  Singapore 449883");
        print("1 Fullerton Square");
        print("545 Orchard Road, #06-19 Far East Shopping Centre, Singapore 238882");
        print("89 Marine Parade Central, #06-750, Singapore 440089");
        print("541 Orchard Road, #01-01A Liat Tower, Singapore 238881");
        print("3B River Valley Road, #01-09/10, Singapore 179021");
        print("1 Tanglin Road, #01-08 Orchard Parade Hotel, Singapore 247905");
        print("What the fuck");
        print("1 Tanglin Road, #01-08");
        print("#01-08 Singapore 570122");

        print("#01-08 S(570122)");
        print("#01-08 SG 570122");

        print("Jalan Bukit Merah Blk 6 Stall No 01-143, Singapore 150006");
        print("Jalan Bukit Merah Blk 6 Stall No 143, Singapore 150006");
        print("Singapore 150006.");
        print("(Singapore 150006)");
        print("'Singapore 150006'");
    }

    @Test
    void single() {
        print("(Singapore 150006)");
    }
}