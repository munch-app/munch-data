package munch.catalyst.sources;

import corpus.data.CorpusData;
import corpus.utils.FieldUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by: Fuxing
 * Date: 6/8/2017
 * Time: 6:02 PM
 * Project: munch-corpus
 */
public final class InstagramSource extends WhitelistSource {
    InstagramSource(String id, double boost) {
        super(id, boost);
    }

    @Override
    public List<SourcedImage> extract(CorpusData data) {
        // Must be correct Corpus
        if (!data.getCorpusName().equals("Global.Instagram.Media")) return Collections.emptyList();

        // Must be correct id
        String userId = FieldUtils.getValue(data, "Instagram.Media.userId");
        if (!StringUtils.equals(userId, id)) return Collections.emptyList();

        // Extract instagram source
        String mediaId = FieldUtils.getValue(data, "Instagram.Media.mediaId");
        Objects.requireNonNull(mediaId);

        Map<String, String> images = FieldUtils.get(data, "Instagram.Media.images")
                .map(CorpusData.Field::getMetadata).orElse(null);
        if (images == null)
            return Collections.emptyList(); // Need change this to null check after instagram corpus is properly updated

        return Collections.singletonList(new SourcedImage(
                "instagram",
                "instagram.com/" + mediaId,
                mediaId,
                images,
                boost));
    }
}
