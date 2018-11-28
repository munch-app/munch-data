package munch.data.client;

import com.typesafe.config.ConfigFactory;
import munch.data.named.NamedQuery;
import munch.restful.client.RestfulClient;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by: Fuxing
 * Date: 28/11/18
 * Time: 10:13 AM
 * Project: munch-data
 */
@Singleton
public final class NamedQueryClient extends RestfulClient {

    @Inject
    public NamedQueryClient() {
        super(ConfigFactory.load().getString("services.munch-data.url"));
    }

    public NamedQuery get(String slug, String version) {
        return doGet("/named/query/:slug/:version")
                .path("slug", slug)
                .path("version", version)
                .asDataObject(NamedQuery.class);
    }

    public void put(NamedQuery namedQuery) {
        doPut("/named/query/:slug/:version")
                .path("slug", namedQuery.getSlug())
                .path("version", namedQuery.getVersion())
                .body(namedQuery)
                .asResponse();
    }
}
