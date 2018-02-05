package munch.data.place.collector;

import corpus.data.CorpusData;
import corpus.field.FieldUtils;

import javax.inject.Singleton;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 14/12/2017
 * Time: 4:27 PM
 * Project: munch-data
 */
@Singleton
public final class ArticleCollector extends AbstractCollector {
    private static final Set<String> BLACKLIST_SOURCE_ID = Set.of(
            "ieatishootipost.sg",
            "ordinarypatrons.com",
            "eatbook.sg",
            "thesmartlocal.com",
            "therantingpanda.com",
            "rubbisheatrubbishgrow.com",
            "thehalalfoodblog.com",
            "hungryangmo.com",
            "six-and-seven.com",
            "allaboutceil.com",
            "missneverfull.com",
            "foodgem.sg"
    );

    @Override
    public List<CollectedImage> collect(String placeId, List<CorpusData> list) {
        return list.stream()
                .filter(this::isArticle)
                .flatMap(data -> data.getFields().stream())
                .filter(field -> field.getKey().equals("Article.image"))
                .map(field -> mapField(field, CollectedImage.From.Article))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private boolean isArticle(CorpusData data) {
        if (!data.getCorpusName().equals("Global.MunchArticle.Article")) return false;
        if (BLACKLIST_SOURCE_ID.contains(FieldUtils.getValue(data, "Article.templateId"))) return false;
        return FieldUtils.get(data, "Article.groupings")
                .map(CorpusData.Field::getValue)
                .filter(s -> s.equals("1"))
                .isPresent();
    }
}
