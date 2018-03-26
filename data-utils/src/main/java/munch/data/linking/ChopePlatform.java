package munch.data.linking;

/**
 * Created by: Fuxing
 * Date: 26/3/2018
 * Time: 6:27 AM
 * Project: munch-data
 */
public class ChopePlatform implements Platform {

    @Override
    public String parse(PlatformUrl url) {
        switch (url.getDomain()) {
            case "cho.pe":
                return wrap("chope.co/shortner/", url.getPath(0));
            case "shop.chope.co":
                if (url.hasPath(2, "products")) {
                    return wrap("chope.co/products/", url.getPath(1));
                }
                return null;
            default:
                return wrap("chope.co/booking/", url.getQueryString("rid"));

        }
    }
}
