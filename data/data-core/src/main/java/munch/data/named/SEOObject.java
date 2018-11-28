package munch.data.named;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * For SEO Objects.
 * <p>
 * Created by: Fuxing
 * Date: 28/11/18
 * Time: 10:02 AM
 * Project: munch-data
 */
public interface SEOObject {
    // SEO Keyword is dead, not using it

    @NotBlank
    @Size(min = 10, max = 100)
    String getTitle();

    void setTitle(String title);

    @NotBlank
    @Size(min = 10, max = 255)
    String getDescription();

    void setDescription(String description);
}
