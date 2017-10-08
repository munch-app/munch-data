package munch.data.clients;

import com.typesafe.config.Config;
import munch.data.Article;
import munch.data.InstagramMedia;
import munch.data.Place;
import munch.restful.client.RestfulClient;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created By: Fuxing Loh
 * Date: 17/4/2017
 * Time: 9:44 PM
 * Project: munch-core
 */
@Singleton
public class DataClient extends RestfulClient {

    /**
     * Look at data service package to api service settings
     */
    @Inject
    public DataClient(Config config) {
        super(config.getString("services.data.url"));
    }

    public void put(Place place, long cycleNo) {
        doPut("/places/{cycleNo}/{id}")
                .path("cycleNo", cycleNo)
                .path("id", place.getId())
                .body(place)
                .asResponse()
                .hasCode(200);
    }

    public void put(InstagramMedia media, long cycleNo) {
        doPut("/places/{placeId}/instagram/medias/{cycleNo}/{mediaId}")
                .path("placeId", media.getPlaceId())
                .path("cycleNo", cycleNo)
                .path("mediaId", media.getMediaId())
                .body(media)
                .hasCode(200);
    }

    public void put(Article article, long cycleNo) {
        doPut("/places/{placeId}/articles/{cycleNo}/{articleId}")
                .path("placeId", article.getPlaceId())
                .path("cycleNo", cycleNo)
                .path("articleId", article.getArticleId())
                .body(article)
                .hasCode(200);
    }

    public void deletePlaces(long cycleNo) {
        doDelete("/places/{cycleNo}/before")
                .path("cycleNo", cycleNo)
                .asResponse()
                .hasCode(200);
    }

    public void deleteArticles(long cycleNo) {
        doDelete("/places/0/articles/{cycleNo}/before")
                .path("cycleNo", cycleNo)
                .asResponse()
                .hasCode(200);
    }

    public void deleteInstagramMedias(long cycleNo) {
        doDelete("/places/0/instagram/medias/{cycleNo}/before")
                .path("cycleNo", cycleNo)
                .asResponse()
                .hasCode(200);
    }
}
