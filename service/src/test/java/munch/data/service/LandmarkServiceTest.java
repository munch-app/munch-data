package munch.data.service;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.google.inject.Guice;
import com.google.inject.Injector;
import munch.data.Location;
import munch.data.client.LandmarkClient;
import munch.data.location.Landmark;
import munch.restful.core.NextNodeList;
import munch.restful.core.exception.ValidationException;
import munch.restful.server.RestfulServer;
import org.junit.jupiter.api.*;

/**
 * Created by: Fuxing
 * Date: 10/7/18
 * Time: 10:08 PM
 * Project: munch-data
 */
class LandmarkServiceTest {
    // docker-compose up must be running to test this

    private static Landmark staticLandmark;

    private static LandmarkClient client;

    @BeforeAll
    static void beforeAll() throws InterruptedException {
        staticLandmark = new Landmark();
        staticLandmark.setLandmarkId("00000000-0000-0000-0000-000000000000");
        staticLandmark.setType(Landmark.Type.train);
        staticLandmark.setName("Test Landmark Name");

        Location location = new Location();
        location.setAddress("Singapore 434434");
        location.setLatLng("1.384334,103.843433");
        location.setCity("singapore");
        location.setCountry("SGP");
        staticLandmark.setLocation(location);

        // Override default port
        System.setProperty("http.port", "4343");

        Injector injector = Guice.createInjector(new TestModule());

        // Setup Tables, Server & Client
        TestModule.setupTables(injector.getInstance(AmazonDynamoDB.class));
        RestfulServer server = RestfulServer.start(injector.getInstance(LandmarkService.class));
        client = new LandmarkClient("http://localhost:"+server.getPort());

        // Wait for Server to be up
        Thread.sleep(2000);
    }

    @BeforeEach
    void setUp() {
        client.put(staticLandmark);
    }

    @AfterEach
    void tearDown() {
        client.iterator().forEachRemaining(landmark -> {
            client.delete(landmark.getLandmarkId());
        });
    }

    @Test
    void list() {
        NextNodeList<Landmark> list = client.list(null, 10);
        Assertions.assertEquals(list.size(), 1);
        assertEquals(list.get(0), staticLandmark);
    }

    @Test
    void get() {
        Landmark landmark = client.get(staticLandmark.getLandmarkId());
        assertEquals(landmark, staticLandmark);
    }

    @Test
    void put() {
        Landmark updated = client.get(staticLandmark.getLandmarkId());
        updated.setName("Updated Name");
        client.put(updated);
        assertEquals(updated, client.get(staticLandmark.getLandmarkId()));
    }

    @Test
    void post() {
        Landmark landmark = new Landmark();
        landmark.setName("Posted Landmark");
        landmark.setType(Landmark.Type.train);
        landmark.setLocation(staticLandmark.getLocation());

        Landmark posted = client.post(landmark);
        Assertions.assertNotNull(posted.getLandmarkId());

        landmark.setLandmarkId(posted.getLandmarkId());
        assertEquals(posted, landmark);
    }

    @Test
    void delete() {
        Landmark deleted = client.delete(staticLandmark.getLandmarkId());
        assertEquals(deleted, staticLandmark);

        deleted = client.get(staticLandmark.getLandmarkId());
        Assertions.assertNull(deleted);
    }

    @Test
    void validation() {
        Assertions.assertThrows(ValidationException.class, () -> {
            Landmark error = client.get(staticLandmark.getLandmarkId());
            error.setName(null);
            client.put(error);
        });
    }

    public static void assertEquals(Landmark left, Landmark right) {
        Assertions.assertEquals(left.getLocation(), right.getLocation());
        Assertions.assertEquals(left.getName(), right.getName());
        Assertions.assertEquals(left.getType(), right.getType());
        Assertions.assertEquals(left.getLandmarkId(), right.getLandmarkId());
    }
}