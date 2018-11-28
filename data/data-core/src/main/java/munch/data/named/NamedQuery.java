package munch.data.named;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import munch.data.location.Area;
import munch.data.tag.Tag;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * This is an continuation of NamedSearchQuery
 * However data structure is now defined by munch-data for consistency
 * <p>
 * Created by: Fuxing
 * Date: 28/11/18
 * Time: 9:38 AM
 * Project: munch-data
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class NamedQuery implements NamedObject, SEOObject {

    private String slug;
    private String version;

    private Long updatedMillis;

    private String title;
    private String description;

    private List<Area> areas;
    private List<Tag> tags;
    private Long count;

    @NotNull
    @Pattern(regexp = "[a-z0-9-]{1,255}")
    @Override
    public String getSlug() {
        return slug;
    }

    @Override
    public void setSlug(String slug) {
        this.slug = slug;
    }

    @NotNull
    @Pattern(regexp = "[0-9]{4}-[0-9]{2}-[0-9]{2}")
    @Override
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @NotNull
    public Long getUpdatedMillis() {
        return updatedMillis;
    }

    public void setUpdatedMillis(Long updatedMillis) {
        this.updatedMillis = updatedMillis;
    }

    @NotBlank
    @Size(min = 10, max = 100)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @NotBlank
    @Size(min = 10, max = 255)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @NotNull
    public List<Area> getAreas() {
        return areas;
    }

    public void setAreas(List<Area> areas) {
        this.areas = areas;
    }

    @NotNull
    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    @NotNull
    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "NamedQuery{" +
                "slug='" + slug + '\'' +
                ", version='" + version + '\'' +
                ", updatedMillis=" + updatedMillis +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", areas=" + areas +
                ", tags=" + tags +
                ", count=" + count +
                '}';
    }
}
