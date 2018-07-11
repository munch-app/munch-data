package munch.data.service;

import munch.data.client.BrandClient;
import munch.data.brand.Brand;
import munch.file.Image;
import munch.restful.core.exception.StructuredException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static munch.data.tag.Tag.Type.Food;

class BrandTest {
    BrandClient testClient;

    BrandTest() {
        testClient = new BrandClient("http://localhost:8010/v4.0");
        Brand brand = createSampleBrand();
        TestHTTP(brand);
    }

    Brand createSampleBrand() {
        Brand brand = new Brand();
        brand.setName("K00LBR4ND");

        Set<String> names = new HashSet<>();

        names.add("name1");
        names.add("name2");
        brand.setNames(names);


        List<Brand.Tag> tags = new ArrayList<>();
        Brand.Tag tag = new Brand.Tag();
        tag.setName("test");
        tag.setTagId("qwertyuiop696969");
        tag.setType(Food);
        tags.add(tag);
        brand.setTags(tags);

        List<Image> images = new ArrayList<Image>();
        brand.setImages(images);
        return brand;
    }

    void TestHTTP(Brand brand) {
        TestPostAndDelete(brand);
        TestGet(brand);

    }

    @Test
    void TestPostAndDelete(Brand brand) {
        Brand postedBrand = testClient.post(brand);
        brand.setBrandId(postedBrand.getBrandId());

        Assertions.assertEquals(postedBrand, brand, "asdad");

        System.out.println("posted: " + postedBrand.getBrandId());
        System.out.println("orig: " + brand.getBrandId());

        System.out.println("POST working: " +
                postedBrand.equals(brand));

        Brand deletedBrand = testClient.delete(postedBrand.getBrandId());

        System.out.println("DELETE working: " +
                deletedBrand.equals(brand));
    }

    @Test
    void TestGet(Brand brand) {
        String storedBrandId = brand.getBrandId();
        Brand storedBrand = testClient.get(storedBrandId);
        System.out.println("GET working: " +
                storedBrand.equals(brand));
    }

    @Test
    void TestPostAndPut(Brand brand) {
        Brand newBrand = testClient.get(brand.getBrandId());
        brand.setName("newName");
        testClient.put(brand);

        System.out.println("PUT working: " +
                    !newBrand.getName().equals(brand.getName()));
    }

}
