package munch.data.place.ml;

import corpus.data.CorpusData;
import corpus.field.AbstractKey;

/**
 * Created by: Fuxing
 * Date: 6/2/18
 * Time: 3:07 PM
 * Project: munch-data
 */
public class TrainingPipelineKey extends AbstractKey {

    /**
     * Use existing field in corpus.field package
     * Or create your own sets of fields
     *
     * @param key   name of field
     * @param multi if field allows multi values
     */
    protected TrainingPipelineKey(String key, boolean multi) {
        super("Sg.Munch.PlaceTagTraining." + key, multi);
    }

    public static Input input = new Input();
    public static Output output = new Output();

    public static class Input extends AbstractKey {
        protected Input() {
            super("Sg.Munch.PlaceTagTraining.input", true);
        }

        public CorpusData.Field create(String value, int count) {
            CorpusData.Field field = createField(value);
            field.getMetadata().put("count", String.valueOf(count));
            return field;
        }
    }

    public static class Output extends AbstractKey {
        protected Output() {
            super("Sg.Munch.PlaceTagTraining.output", true);
        }
    }
}
