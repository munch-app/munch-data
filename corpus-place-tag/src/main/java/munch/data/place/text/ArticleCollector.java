package munch.data.place.text;

import com.fasterxml.jackson.databind.JsonNode;
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
    private static final String ARTICLE_TEXT = "Global.MunchArticle.ArticleText.Content";
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
        return data.getCorpusName().equals("Global.MunchArticle.Article");
    }

    private List<String> getTexts(CorpusData data) {
        String articleId = FieldUtils.getValueOrThrow(data, "Article.articleId");
        String ordering = FieldUtils.getValueOrThrow(data, "Article.articleListNo");

        JsonNode node = documentClient.get(ARTICLE_TEXT, articleId, ordering);
        if (node == null) return List.of();

        if (node.isArray()) return JsonUtils.toList(node, String.class);
        return List.of(node.asText());
    }
}
