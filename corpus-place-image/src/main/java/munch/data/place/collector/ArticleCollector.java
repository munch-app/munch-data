package munch.data.place.collector;

import corpus.data.CorpusData;
import corpus.field.FieldUtils;
import corpus.field.MetaKey;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Singleton;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by: Fuxing
 * Date: 14/12/2017
 * Time: 4:27 PM
 * Project: munch-data
 */
@Singleton
public final class ArticleCollector extends AbstractCollector {

    @Override
    public List<CollectedImage> collect(String placeId, List<CorpusData> list) {
        return list.stream()
                .filter(this::isArticle)
                .flatMap(this::collectImages)
                .collect(Collectors.toList());
    }

    private boolean isArticle(CorpusData data) {
        return data.getCorpusName().equals("Global.MunchArticle.Article");
    }

    private Stream<CollectedImage> collectImages(CorpusData data) {
        CollectedImage.From from = getFrom(data);
        if (from == null) return Stream.empty();

        return data.getFields().stream()
                .map(field -> mapField(field, from))
                .filter(Objects::nonNull);
    }

    private static CollectedImage.From getFrom(CorpusData data) {
        String grouping = FieldUtils.getValue(data, "Article.groupings");

        if (StringUtils.equals(grouping, "1")) {
            return CollectedImage.From.ArticleFullPage;
        } else {
            if (MetaKey.version.isEqualAfter(data, "2018-03-28")) {
                return CollectedImage.From.ArticleListPage;
            }
            return null;
        }
    }

    @Override
    protected CollectedImage mapField(CorpusData.Field field, CollectedImage.From from) {
        if (field.getKey().equals("Article.image.doc")) {
            return super.mapField(field, CollectedImage.From.ArticleFullPageDoc);
        }

        if (field.getKey().equals("Article.image")) {
            return super.mapField(field, from);
        }

        return null;
    }
}
