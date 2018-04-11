package munch.data.place.elastic;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.ecs.AmazonECS;
import com.amazonaws.services.ecs.AmazonECSClientBuilder;
import com.amazonaws.services.ecs.model.*;
import com.google.common.io.Resources;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.typesafe.config.Config;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.indices.CreateIndex;
import munch.restful.WaitFor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 9/10/2017
 * Time: 2:04 AM
 * Project: munch-data
 */
public final class GraphElasticModule extends AbstractModule {
    private static final Logger logger = LoggerFactory.getLogger(GraphElasticModule.class);

    @Override
    protected void configure() {
        requestInjection(this);
    }

    @Inject
    void configureMapping(@Named("munch.data.place.jest") JestClient client) throws IOException, InterruptedException {
        Thread.sleep(1500);

        logger.info("Creating index");
        createIndex(client);

        Thread.sleep(3000);
    }

    private static void createIndex(JestClient client) throws IOException {
        URL url = Resources.getResource("search-index.json");
        String json = Resources.toString(url, Charset.forName("UTF-8"));
        JestResult result = client.execute(new CreateIndex.Builder("graph").settings(json).build());
        logger.info("Created index result: {}", result.getJsonString());
    }

    /**
     * Wait for elastic to be started
     *
     * @return elastic RestClient
     */
    @Provides
    @Singleton
    @Named("munch.data.place.jest")
    JestClient provideClient(@Named("munch.data.place.jest.url") String url) {
        WaitFor.host(url, Duration.ofSeconds(180));

        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig.Builder(url)
                .multiThreaded(true)
                .defaultMaxTotalConnectionPerRoute(5)
                .readTimeout(30000)
                .connTimeout(15000)
                .build());
        return factory.getObject();
    }

    @Provides
    @Singleton
    @Named("munch.data.place.jest.url")
    String provideElasticUrl(Config config) {
        ECSLocator ecsLocator = new ECSLocator(config);
        return ecsLocator.locateUrl();
    }

    private class ECSLocator {

        private final String clusterArn;
        private final String taskDefinition;
        private final AmazonECS ecsClient;

        public ECSLocator(Config config) {
            this.clusterArn = config.getString("service.elastic.ecs.clusterArn");
            this.taskDefinition = config.getString("service.elastic.ecs.taskDefinition");
            this.ecsClient = AmazonECSClientBuilder.standard()
                    .withRegion(config.getString("service.elastic.ecs.region"))
                    .withCredentials(new DefaultAWSCredentialsProviderChain())
                    .build();
        }

        private String locateUrl() {
            Task task = getTask();
            if (task == null) throw new IllegalStateException("Task is required to locate ecs for elastic");

            List<Container> containers = task.getContainers();
            for (Container container : containers) {
                //noinspection LoopStatementThatDoesntLoop
                for (NetworkBinding networkBinding : container.getNetworkBindings()) {
                    String ip = networkBinding.getBindIP();
                    int port = networkBinding.getHostPort();
                    return "http://" + ip + ":" + port;
                }
            }

            throw new IllegalStateException("Failed to find network binding");
        }

        private Task getTask() {
            List<Task> tasks = getTasks();
            if (tasks.isEmpty()) return null;
            if (tasks.size() > 1) throw new IllegalStateException("Task size more then 1");
            return tasks.get(0);
        }

        private List<Task> getTasks() {
            ListTasksRequest request = new ListTasksRequest();
            request.setCluster(clusterArn);
            request.setFamily(taskDefinition);
            request.setDesiredStatus(DesiredStatus.RUNNING);

            List<Task> tasks = new ArrayList<>();

            do {
                ListTasksResult result = ecsClient.listTasks(request);
                request.setNextToken(result.getNextToken());
                tasks.addAll(getTasks(result.getTaskArns()));

                if (result.getNextToken() == null) {
                    // If no more next token, end of pagination
                    return tasks;
                }
            } while (true);
        }

        private List<Task> getTasks(List<String> taskArns) {
            if (taskArns.isEmpty()) return Collections.emptyList();

            DescribeTasksRequest request = new DescribeTasksRequest();
            request.setCluster(clusterArn);
            request.setTasks(taskArns);
            DescribeTasksResult result = ecsClient.describeTasks(request);
            return result.getTasks();
        }
    }
}
