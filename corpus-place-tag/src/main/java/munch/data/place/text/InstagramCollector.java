package munch.data.place.text;

import corpus.data.CorpusData;
import munch.corpus.instagram.InstagramMedia;
import munch.corpus.instagram.InstagramMediaClient;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 14/12/2017
 * Time: 4:28 PM
 * Project: munch-data
 */
@Singleton
public final class InstagramCollector extends AbstractCollector {

    private final InstagramMediaClient instagramMediaClient;

    @Inject
    public InstagramCollector(InstagramMediaClient instagramMediaClient) {
        this.instagramMediaClient = instagramMediaClient;
    }

    @Override
    public List<CollectedText> collect(String placeId, List<CorpusData> list) {
        // Only first 20 will be processed
        List<InstagramMedia> mediaList = instagramMediaClient.listByPlace(placeId, null, null, 20);
        return mediaList.stream()
                .map(media -> {
                    CollectedText text = new CollectedText();
                    text.setUniqueId("instagram|" + media.getMediaId());
                    text.setFrom(CollectedText.From.Instagram);
                    text.setContent(media.getCaption());
                    return text;
                })
                .collect(Collectors.toList());
    }
}