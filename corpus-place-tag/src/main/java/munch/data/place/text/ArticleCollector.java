package munch.data.place.text;

import corpus.data.CorpusData;
import corpus.data.DocumentClient;
import corpus.field.FieldUtils;
import munch.restful.core.JsonUtils;

import javax.inject.Inject;
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
    private static final String ARTICLE_TEXT = "Global.MunchArticle.ArticleText";
    private final DocumentClient documentClient;

    @Inject
    public ArticleCollector(DocumentClient documentClient) {
        this.documentClient = documentClient;
    }

    @Override
    public List<CollectedText> collect(String placeId, List<CorpusData> list) {
        return list.stream()
                .filter(this::isArticle)
                .map(this::getTexts)
                .map(texts -> mapField(texts, CollectedText.From.Article))
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

    private List<String> getTexts(CorpusData data) {
        return FieldUtils.get(data, "Article.articleId")
                .map(field -> documentClient.get(ARTICLE_TEXT, field.getValue()))
                .map(node -> JsonUtils.toList(node, String.class))
                .orElse(List.of());
    }
}
