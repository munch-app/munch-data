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
        asserts("chope.co/booking/depizza1803dpz", "https://book.chope.co/booking?rid=depizza1803dpz&source=sethlui");
        asserts("chope.co/booking/twoblurguys1601tbg", "https://book.chope.co/booking?rid=twoblurguys1601tbg&source=ivantehrunningman");
        asserts("chope.co/booking/cajunonwheelsplazasingapura1710cow", "https://bookv5.chope.co/booking?rid=cajunonwheelsplazasingapura1710cow&source=rest_cajunonwheels.com%2Fsingapore&d=0&t=0&a=0&c=0&hn=0&hh=0&hh2=0&hf=0&select_location=0&sub_source=google_map&txn_ref=0&smart=0&hft=0&hs=0&go=0");
        asserts("chope.co/booking/odette20dsu19r14", "https://bookv5.chope.co/booking?rid=odette20dsu19r14&source=Odette");
        asserts("chope.co/shortner/dineatcajunonwheelsplazasingapurasg", "https://cho.pe/dineatcajunonwheelsplazasingapurasg");

        asserts("chope.co/products/cafe-mosaic-1-for-1-buffet-buffet-licious-deals", "https://shop.chope.co/products/cafe-mosaic-1-for-1-buffet-buffet-licious-deals?aff=95&utm_source=SethLui&utm_medium=article_march&utm_campaign=Buffetlicious");
    }

    @Test
    void facebook() {
        asserts("facebook.com/ottomankebabandgrill", "https://www.facebook.com/ottomankebabandgrill/");
        asserts("facebook.com/ottomankebabandgrill", "https://www.facebook.com/ottomankebabandgrill");
        asserts("facebook.com/ottomankebabandgrill", "https://www.facebook.com/ottomankebabandgrill?bar=foo");
        asserts("facebook.com/castironsg", "https://www.facebook.com/castironsg/");
        asserts("facebook.com/StrangersReunion", "https://www.facebook.com/StrangersReunion/");

        asserts(null, "https://www.facebook.com/");
    }

    @Test
    void quandoo() throws Exception {
        asserts("quandoo.sg/reservation/mGuVG", "https://reservation.quandoo.sg/widget/reservation/merchant/mGuVG?aid=54");
        asserts("quandoo.sg/reservation/xHUk0", "https://widget.quandoo.sg/widget/reservation/merchant/xHUk0?aid=2&countryId=SGP");
        asserts("quandoo.sg/reservation/39074", "https://booking-widget.quandoo.sg/iframe.html?agentId=2&merchantId=39074&origin=http%3A%2F%2Fdev.quandoodrafts.com&path=https%3A%2F%2Fbooking-widget.quandoo.com%2F");
        asserts("quandoo.sg/place/edge-food-theatre-14782", "https://www.quandoo.sg/place/edge-food-theatre-14782?TC=EN_SG_SCI_10000001_90000001&gclid=EAIaIQobChMI6Kfa-7aJ2gIVUYpoCh27fgznEAEYASABEgJ7CfD_BwE");
    }

    @Test
    void oddle() throws Exception {
        asserts("oddle.me/site/jinjja-chicken", "http://jinjja-chicken.oddle.me");
    }

    @Test
    void foodpanda() throws Exception {
        asserts("foodpanda.sg/restaurant/s4qv", "https://www.foodpanda.sg/restaurant/s4qv/pure-juice");
        asserts("foodpanda.sg/restaurant/s0cs", "https://www.foodpanda.sg/restaurant/s0cs/seed");
    }

    @Test
    void zomato() throws Exception {
        asserts("zomato.com/manila/little-winehaus-paligsahan-quezon-city", "https://www.zomato.com/manila/little-winehaus-paligsahan-quezon-city");
    }

    @Test
    void deliveroo() throws Exception {
        asserts("deliveroo.com/singapore/tanjong-pagar/seed-and-soil", "https://deliveroo.com.sg/menu/singapore/tanjong-pagar/seed-and-soil");
        asserts("deliveroo.com/singapore/kovan/pure-juice", "https://deliveroo.com.sg/menu/singapore/kovan/pure-juice");
    }

    @Test
    void instagram() throws Exception {
        asserts("instagram.com/DanielFoodDiary", "https://www.instagram.com/DanielFoodDiary");
    }

    @Test
    void ubereats() throws Exception {
        asserts("ubereats.com/stores/2a6866e2-7ddd-4b5c-9c73-f4acfb82f9bf", "https://www.ubereats.com/stores/2a6866e2-7ddd-4b5c-9c73-f4acfb82f9bf");
        asserts("ubereats.com/stores/2a6866e2-7ddd-4b5c-9c73-f4acfb82f9bf", "https://www.ubereats.com/singapore/food-delivery/project-acai-holland-village/Kmhm4n3dS1ycc_Ss-4L5vw/");
    }

    @Test
    void feastbump() throws Exception {
        asserts("feastbump.com/park-bench-deli", "http://www.feastbump.com/menus/park-bench-deli");
    }

    @Test
    void hgw() throws Exception {
        asserts("hungrygowhere.com/singapore/sushi-jiro", "https://www.hungrygowhere.com/singapore/sushi-jiro/book-now/");
        asserts("hungrygowhere.com/singapore/sushi-jiro", "https://www.hungrygowhere.com/singapore/sushi-jiro/");
        asserts("hungrygowhere.com/5a7bbbe8e4b0de23fa3aa5e3","https://reservations.hungrygowhere.com/the-lobby-lounge-at-shangri-la-hotel/?widget_id=53ffe4fcf30562d559000000&restaurant_id=5a7bbbe8e4b0de23fa3aa5e3&partner_code=sgfoodonfoot&partner_auth=09011348-3F4C-4BB8-89E9-AE30B361938A");
    }

    @Test
    void eatigo() throws Exception {
        asserts("eatigo.com/sg/restaurant/653","https://www.eatigo.com/home/index.php/sg/en/restaurant/id/653/from/DFD/nhd/yes/");
        asserts("eatigo.com/sg/restaurant/653","https://www.eatigo.com/home/index.php/sg/en/restaurant/id/653");
    }

    @Test
    void yelp() throws Exception {
        asserts("yelp.com/biz/sungei-road-laksa-singapore-2", "https://www.yelp.com.sg/biz/sungei-road-laksa-singapore-2?frvs=True");
    }

    @Test
    void burpple() throws Exception {
        asserts("burrple.com/place/fatty-ox-hong-kong-kitchen", "https://www.burpple.com/fatty-ox-hong-kong-kitchen");
    }

    @Test
    void foursquare() throws Exception {
        asserts("foursquare.com/v/5324821311d2391c01c3400f", "https://foursquare.com/v/percolate/5324821311d2391c01c3400f");
    }

    @Test
    void honestbee() throws Exception {
        asserts("honestbee.sg/restaurants/ministry-of-pasta-and-grill", "https://www.honestbee.sg/en/food/restaurants/ministry-of-pasta-and-grill");
    }

    @Test
    void google() throws Exception {
        asserts(null, "https://www.google.com/maps/place/1+Netheravon+Rd,+Singapore+508502/@1.3905472,103.9837808,17z/data=!3m1!4b1!4m5!3m4!1s0x31da3c1793de83fb:0x2c2b0933f65b90f0!8m2!3d1.3905472!4d103.9859695");
        asserts(null, "https://maps.google.com/?q=1+Netheravon+Rd,+Changi&entry=gmail&source=g");
    }
}