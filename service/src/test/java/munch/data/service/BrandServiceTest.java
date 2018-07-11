package munch.data.service;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.google.inject.Guice;
import com.google.inject.Injector;
import munch.data.brand.Brand;
import munch.data.brand.Brand.Tag;
import munch.data.client.BrandClient;
import munch.file.Image;
import munch.restful.core.NextNodeList;
import munch.restful.core.exception.ValidationException;
import munch.restful.server.RestfulServer;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by: Bing Hwang
 * Date: 11/7/18
 * Time: 3:07 PM
 * Project: munch-data
 */
public class BrandServiceTest {

    private static Brand staticBrand;

    private static BrandClient client;

    @BeforeAll
    static void beforeAll() throws InterruptedException {
        staticBrand = new Brand();
        staticBrand.setBrandId("00000000-0000-0000-0000-000000000000");
        staticBrand.setName("Test Brand Name");

        Set<String> staticBrandNames = new HashSet<>();
        staticBrandNames.add("Test Brand Name One");
        staticBrandNames.add("Test Brand Name Two");
        staticBrand.setNames(staticBrandNames);

        List<Tag> staticTags = new ArrayList<>();
        staticBrand.setTags(staticTags);

        List<Image> staticImages = new ArrayList<>();
        staticBrand.setImages(staticImages);

        System.setProperty("http.port", "4343");

        Injector injector = Guice.createInjector(new TestModule());

        TestModule.setupTables(injector.getInstance(AmazonDynamoDB.class));
        RestfulServer server = RestfulServer.start(injector.getInstance(BrandService.class));
        client = new BrandClient("http://localhost:"+server.getPort());

        Thread.sleep(2000);

    }

    @BeforeEach
    void setUp() { client.put(staticBrand); }

    @AfterEach
    void tearDown() {
        client.iterator().forEachRemaining(brand -> {
            client.delete(brand.getBrandId());
        });
    }

    @Test
    void list() {
        NextNodeList<Brand> list = client.list(null, 10);
        Assertions.assertEquals(list.size(), 1);
        assertEquals(list.get(0), staticBrand);
    }

    @Test
    void get() {
        Brand brand = client.get(staticBrand.getBrandId());
        assertEquals(brand, staticBrand);
    }

    @Test
    void put() {
        Brand updated = client.get(staticBrand.getBrandId());
        updated.setName("Updated Name");
        client.put(updated);
        assertEquals(updated, client.get(staticBrand.getBrandId()));
    }

    @Test
    void post() {
        Brand brand = new Brand();

        brand.setName("PostName");

        Set<String> postNames = new HashSet<String>();
        postNames.add("Post Names 1");
        postNames.add("Post Names 2");
        brand.setNames(postNames);

        List<Tag> postTags = new ArrayList<Tag>();
        brand.setTags(postTags);

        List<Image> postImages = new ArrayList<Image>();
        brand.setImages(postImages);

        Brand posted = client.post(brand);
        Assertions.assertNotNull(posted.getBrandId());

        brand.setBrandId(posted.getBrandId());
        assertEquals(posted, brand);
    }

    @Test
    void delete() {
        Brand deleted = client.delete(staticBrand.getBrandId());
        assertEquals(deleted, staticBrand);

        deleted = client.get(staticBrand.getBrandId());
        Assertions.assertNull(deleted);
    }

    @Test
    void validation() {
        Assertions.assertThrows(ValidationException.class, () -> {
            Brand error = client.get(staticBrand.getBrandId());
            error.setName(null);
            client.put(error);
        });
    }

    public static void assertEquals(Brand left, Brand right) {
        Assertions.assertEquals(left.getName(), right.getName());
        Assertions.assertEquals(left.getNames(), right.getNames());
        Assertions.assertEquals(left.getTags(), right.getTags());
        Assertions.assertEquals(left.getImages(), right.getImages());
        Assertions.assertEquals(left.getBrandId(), right.getBrandId());
    }

}
