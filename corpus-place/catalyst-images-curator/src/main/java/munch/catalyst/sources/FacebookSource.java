package munch.catalyst.sources;

import corpus.blob.ImageMapper;
import corpus.blob.ImageMeta;
import corpus.data.CorpusData;
import corpus.field.PlaceKey;

import java.util.Collections;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 28/8/2017
 * Time: 1:14 AM
 * Project: munch-corpus
 */
public class FacebookSource extends WhitelistSource {
    private final ImageMapper imageMapper;


    protected FacebookSource(ImageMapper imageMapper) {
        super("facebook.com", 1);
        this.imageMapper = imageMapper;
    }

    @Override
    public List<SourcedImage> extract(CorpusData data) {
        // Must be correct Corpus
        if (!data.getCorpusName().equals("Global.Facebook.Place")) return Collections.emptyList();

        // Extract facebook image
        String imageUrl = PlaceKey.image.getValue(data);
        if (imageUrl == null) return Collections.emptyList();

        ImageMeta imageMeta = imageMapper.resolveUrl(imageUrl);
        if (imageMeta == null) return Collections.emptyList();

        return Collections.singletonList(new SourcedImage(
                "facebook",
                imageUrl,
                imageMeta.getKey(),
                imageMeta.getImagesAsSizeUrl(),
                boost));
    }
}
