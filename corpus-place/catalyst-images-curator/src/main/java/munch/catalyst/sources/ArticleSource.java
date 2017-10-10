package munch.catalyst.sources;

import corpus.blob.ImageMapper;
import corpus.blob.ImageMeta;
import corpus.data.CorpusData;
import corpus.utils.FieldUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 6/8/2017
 * Time: 6:02 PM
 * Project: munch-corpus
 */
public final class ArticleSource extends WhitelistSource {
    private final ImageMapper imageMapper;

    ArticleSource(String id, double boost, ImageMapper imageMapper) {
        super(id, boost);
        this.imageMapper = imageMapper;
    }

    @Override
    public List<SourcedImage> extract(CorpusData data) {
        // Must be correct Corpus
        if (!data.getCorpusName().equals("Global.Munch.Article")) return Collections.emptyList();

        // Must be correct id
        String templateId = FieldUtils.getValue(data, "Article.Template.id");
        if (!StringUtils.equals(templateId, id)) return Collections.emptyList();

        // Must not be multi place image
        String multiPlace = FieldUtils.getValue(data, "Article.multiPlace");
        if (multiPlace == null) FieldUtils.getValue(data, "Article.multiKey");
        if (Boolean.parseBoolean(multiPlace)) return Collections.emptyList();

        // Extract thumbnail image
        String thumbnailUrl = FieldUtils.getValue(data, "Article.thumbnail");
        if (thumbnailUrl == null) return Collections.emptyList();

        // Must exist in cache
        ImageMeta imageMeta = imageMapper.resolveUrl(thumbnailUrl);
        if (imageMeta == null) return Collections.emptyList();

        return Collections.singletonList(new SourcedImage(
                "article",
                thumbnailUrl,
                imageMeta.getKey(),
                imageMeta.getImagesAsSizeUrl(),
                boost));
    }
}
