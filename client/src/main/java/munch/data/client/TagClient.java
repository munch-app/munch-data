package munch.data.client;

import com.typesafe.config.ConfigFactory;
import munch.restful.client.RestfulClient;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by: Fuxing
 * Date: 30/5/18
 * Time: 2:59 PM
 * Project: munch-data
 */
@Singleton
public final class TagClient extends RestfulClient {

    @Inject
    public TagClient() {
        this(ConfigFactory.load().getString("services.tag.url"));
    }

    public TagClient(String url) {
        super(url);
    }

    // TODO Client Methods
}
