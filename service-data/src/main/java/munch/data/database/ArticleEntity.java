package munch.data.database;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import munch.data.Article;
import munch.data.database.hibernate.PojoUserType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;

/**
 * Created by: Fuxing
 * Date: 18/8/2017
 * Time: 1:20 AM
 * Project: munch-core
 */
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@TypeDefs(value = {
        @TypeDef(name = "articleData", typeClass = ArticleEntity.ArticleUserType.class)
})
@Table(indexes = {
        // Cluster Index: index_munch_article_entity_place_id_sort_key = (placeId, sortKey desc)
        @Index(name = "index_munch_article_entity_cycle_no", columnList = "cycleNo"),
})
public final class ArticleEntity implements AbstractEntity<Article> {
    private Long cycleNo;

    private String placeId;
    private String articleId; // Is Unique
    private String sortKey;

    private Article data;

    @Column(columnDefinition = "CHAR(36)", nullable = false, updatable = false)
    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    /**
     * @return SHA256 hex value of (url + address)
     */
    @Id
    @Column(columnDefinition = "CHAR(64)", nullable = false, updatable = false)
    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    @Column(nullable = false)
    public String getSortKey() {
        return sortKey;
    }

    public void setSortKey(String sortKey) {
        this.sortKey = sortKey;
    }

    public Long getCycleNo() {
        return cycleNo;
    }

    public void setCycleNo(Long cycleNo) {
        this.cycleNo = cycleNo;
    }

    @Type(type = "articleData")
    @Column(nullable = false)
    public Article getData() {
        return data;
    }

    public void setData(Article data) {
        this.data = data;
    }

    public final static class ArticleUserType extends PojoUserType<Article> {
        public ArticleUserType() {
            super(Article.class);
        }
    }
}
