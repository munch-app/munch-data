package munch.data.exceptions;

import munch.restful.core.exception.ExceptionParser;
import munch.restful.core.exception.StructuredException;

/**
 * Created by: Fuxing
 * Date: 12/12/2017
 * Time: 9:42 AM
 * Project: munch-data
 */
public final class ClusterBlockException extends StructuredException {

    static {
        ExceptionParser.register(ClusterBlockException.class, ClusterBlockException::new);
    }

    public ClusterBlockException(StructuredException e) {
        super(e);
    }

    public ClusterBlockException() {
        super(503, ClusterBlockException.class, "http://docs.aws.amazon.com/elasticsearch-service/latest/developerguide/aes-handling-errors.html#aes-handling-errors-watermark");
    }
}
