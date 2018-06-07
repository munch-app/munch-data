package munch.data;

import com.google.common.collect.Iterators;
import corpus.airtable.AirtableApi;
import corpus.airtable.AirtableRecord;
import corpus.engine.AbstractEngine;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.function.Supplier;

/**
 * Created by: Fuxing
 * Date: 5/6/18
 * Time: 4:55 PM
 * Project: munch-data
 */
public abstract class AirtableBridge<T extends ElasticObject> extends AbstractEngine<Object> {

    protected final AirtableApi.Table table;
    private final Supplier<Iterator<T>> supplier;

    /**
     * @param logger   for engine
     * @param table    for airtable
     * @param supplier for server listing
     */
    public AirtableBridge(Logger logger, AirtableApi.Table table, Supplier<Iterator<T>> supplier) {
        super(logger);
        this.table = table;
        this.supplier = supplier;
    }

    @Override
    protected Iterator<Object> fetch(long cycleNo) {
        return Iterators.concat(table.select(), supplier.get());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void process(long cycleNo, Object data, long processed) {
        if (data instanceof AirtableRecord) {
            AirtableRecord record = (AirtableRecord) data;
            processAirtable(record, parse(record));
        } else {
            processServer((T) data);
        }

        sleep(1000);
    }

    protected abstract void processServer(T data);

    protected abstract void processAirtable(AirtableRecord record, T data);

    protected abstract T parse(AirtableRecord record);
}
