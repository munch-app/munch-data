package munch.data.place.topic;


import cc.mallet.pipe.*;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.*;
import com.google.common.io.Resources;

import javax.inject.Singleton;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by Fuxing
 * Date: 6/4/2015
 * Time: 11:51 AM
 * Project: puffin-neuro-learner
 */
@Singleton
public class TopicAnalysis {

    // Number of iterations to run through
    private static final int ITERATIONS = 200;
    private static final int THREADS = 1;

    private final List<Pipe> pipeList;

    public TopicAnalysis() throws IOException {
        ParallelTopicModel.logger.setFilter(record -> !record.getLevel().equals(Level.INFO));

        pipeList = new ArrayList<>();

        pipeList.add(new CharSequenceLowercase());
        pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));

        pipeList.add(new TokenSequenceRemoveStopwords(false, false)
                .addStopWords(readStringArray("stopword-food.txt"))
                .addStopWords(readStringArray("stopword-lang.txt"))
        );
        pipeList.add(new TokenSequence2FeatureSequence());
    }

    private String[] readStringArray(String resourceName) throws IOException {
        URL url = Resources.getResource(resourceName);
        List<String> strings = Resources.readLines(url, Charset.defaultCharset());
        return strings.toArray(new String[strings.size()]);
    }

    /**
     * Apply topic analysis with given number of topics and given number of words
     *
     * @param strings   strings to analyst
     * @param numTopics topics to output
     * @param numWords  number of words per topics
     * @return topics
     * @throws IOException
     */
    public List<Map<String, Integer>> apply(String[] strings, int numTopics, int numWords) throws IOException {
        List<String> inputs = Arrays.stream(strings).map(s1 -> s1.replaceAll("\\s+", " ")).collect(Collectors.toList());
        return analysis(inputs, numTopics, numWords);
    }

    /**
     * Apply topic analysis with given number of topics and given number of words
     *
     * @param strings   strings to analyst
     * @param numTopics topics to output
     * @param numWords  number of words per topics
     * @return topics
     * @throws IOException
     */
    public List<Map<String, Integer>> apply(List<String> strings, int numTopics, int numWords) throws IOException {
        List<String> inputs = strings.stream().map(s1 -> s1.replaceAll("\\s+", " ")).collect(Collectors.toList());
        return analysis(inputs, numTopics, numWords);
    }

    /**
     * Apply topic analysis with given number of topics and given number of words
     *
     * @param inputs    strings to analyst
     * @param numTopics topics to output
     * @param numWords  number of words per topics
     * @return topics
     * @throws IOException
     */
    protected List<Map<String, Integer>> analysis(List<String> inputs, int numTopics, int numWords) throws IOException {
        // Input and output
        List<Map<String, Integer>> output = new ArrayList<>();

        // Load data to their instances
        final InstanceList instances = new InstanceList(new SerialPipes(pipeList));
        inputs.forEach(s -> instances.addThruPipe(new Instance(s, "target", "name", "source")));

        // Create a model, alpha_t = 1.0, beta_w = 0.01
        // Note that the first parameter is passed as the sum over topics, while
        // the second is the parameter for a single dimension of the Dirichlet prior.
        final ParallelTopicModel model = new ParallelTopicModel(numTopics, 1.0, 0.01);
        model.addInstances(instances);

        // Use n parallel samplers, where each one looks at one half the corpus and combine statistics after every iteration.
        model.setNumThreads(THREADS);

        // Run the model for n iterations and stop
        model.setNumIterations(ITERATIONS);
        model.estimate();

        if (model.getData().size() <= 0) {
            return output;
        }

        // The data alphabet maps word IDs to strings
        Alphabet dataAlphabet = instances.getDataAlphabet();

        FeatureSequence tokens = (FeatureSequence) model.getData().get(0).instance.getData();
        LabelSequence topics = model.getData().get(0).topicSequence;

        Formatter out = new Formatter(new StringBuilder(), Locale.US);
        for (int position = 0; position < tokens.getLength(); position++) {
            out.format("%s-%d ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)), topics.getIndexAtPosition(position));
        }

        // Estimate the topic distribution of the first instance,
        //  given the current Gibbs state.
        double[] topicDistribution = model.getTopicProbabilities(0);

        // Get an array of sorted sets of word ID/count pairs
        ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();


        // Show top numWords words in topics with proportions for the first document
        for (int topic = 0; topic < numTopics; topic++) {

            HashMap<String, Integer> map = new HashMap<>();
            Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();

            out = new Formatter(new StringBuilder(), Locale.US);
            out.format("%d\t%.3f\t", topic, topicDistribution[topic]);
            int rank = 0;
            while (iterator.hasNext() && rank < numWords) {
                IDSorter idCountPair = iterator.next();
                out.format("%s (%.0f) ", dataAlphabet.lookupObject(idCountPair.getID()), idCountPair.getWeight());
                map.put(dataAlphabet.lookupObject(idCountPair.getID()).toString(), (int) idCountPair.getWeight());
                rank++;
            }

            ValueComparator bvc = new ValueComparator(map);
            TreeMap<String, Integer> sortedMap = new TreeMap<>(bvc);
            sortedMap.putAll(map);
            output.add(sortedMap);
        }

        return output;
    }

    /**
     * For sorting map by value
     */
    private class ValueComparator implements Comparator<String> {

        private Map<String, Integer> base;

        public ValueComparator(Map<String, Integer> base) {
            this.base = base;
        }

        public int compare(String a, String b) {
            return base.get(b).compareTo(base.get(a));
        }
    }
}
