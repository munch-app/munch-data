package munch.data.linking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by: Fuxing
 * Date: 24/3/18
 * Time: 11:50 AM
 * Project: munch-data
 */
class LinkingExtractorTest {

    LinkingExtractor extractor = new LinkingExtractor();

    void asserts(String id, String url) {
        String linkId = extractor.extract(url);
        Assertions.assertEquals(id, linkId);
    }

    @Test
    void chope() {
        asserts("chope.co/booking/wildhoney78bbvza-4", "https://bookv5.chope.co/booking?rid=wildhoney78bbvza-4&source=flashgiveaway_visa&sub_source=adv_sethlui_advertorial");
        asserts("chope.co/booking/centralthai1704cen", "https://bookv5.chope.co/booking?rid=centralthai1704cen&source=chope.co.cms2page&d=0&t=0&a=0&c=0&hn=0&hh=0&hh2=0&hf=0&select_location=0&sub_source=adv_sethlui_advertorial&txn_ref=0&smart=0&hft=0&hs=0&go=0");
        asserts("chope.co/booking/bluelotuschinesenoodlebar1706cnb", "https://book.chope.co/booking/?rid=bluelotuschinesenoodlebar1706cnb&source=citynomads");
        asserts("chope.co/booking/centralthai1704cen", "https://shop.chope.co/products/cafe-mosaic-1-for-1-buffet-buffet-licious-deals?aff=95&utm_source=SethLui&utm_medium=article_march&utm_campaign=Buffetlicious");
    }

    @Test
    void facebook() {
        asserts("facebook.com/place/ottomankebabandgrill", "https://www.facebook.com/ottomankebabandgrill/");
        asserts("facebook.com/place/ottomankebabandgrill", "https://www.facebook.com/ottomankebabandgrill");
        asserts("facebook.com/place/ottomankebabandgrill", "https://www.facebook.com/ottomankebabandgrill?bar=foo");
    }
}