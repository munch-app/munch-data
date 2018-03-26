package munch.data.linking;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by: Fuxing
 * Date: 26/3/2018
 * Time: 6:27 AM
 * Project: munch-data
 */
public class QuandooPlatform implements Platform {

    @Override
    public String parse(PlatformUrl url) {
        if (StringUtils.isBlank(url.getPath())) return null;

        if (url.hasPath("iframe.html")) {
            return wrap("quandoo.sg/reservation/", url.getQueryString("merchantId"));
        }

        if (url.hasPath(2,"place")) {
            return wrap("quandoo.sg/place/", url.getPath(1));
        }

        if (url.hasPath(4, "widget", "reservation", "merchant")) {
            return wrap("quandoo.sg/reservation/", url.getPath(3));
        }
        return null;
    }
}
