package munch.data.place.text;

import corpus.data.CorpusData;
import corpus.field.FieldUtils;

import javax.inject.Singleton;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 14/12/2017
 * Time: 4:27 PM
 * Project: munch-data
 */
@Singleton
public final class ArticleCollector extends AbstractCollector {

    @Override
    public List<CollectedText> collect(String placeId, List<CorpusData> list) {
        return list.stream()
                .filter(this::isArticle)
                // TODO Text is not saved in corpus
                .flatMap(data -> data.getFields().stream())
                .filter(field -> field.getKey().equals("Article.image"))
                .map(field -> mapField(field, CollectedText.From.Article))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private boolean isArticle(CorpusData data) {
        if (!data.getCorpusName().equals("Global.MunchArticle.Article")) return false;
        return FieldUtils.get(data, "Article.groupings")
                .map(CorpusData.Field::getValue)
                .filter(s -> s.equals("1"))
                .isPresent();
    }
}
