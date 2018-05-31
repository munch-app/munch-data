package munch.data.exception;

import munch.restful.core.exception.ExceptionParser;
import munch.restful.core.exception.StructuredException;
import munch.restful.core.exception.TimeoutException;

import java.net.SocketTimeoutException;

/**
 * Created by: Fuxing
 * Date: 10/10/2017
 * Time: 11:15 AM
 * Project: munch-data
 */
public final class ElasticException extends StructuredException {

    static {
        ExceptionParser.register(ElasticException.class, ElasticException::new);
    }

    public ElasticException(StructuredException e) {
        super(e);
    }

    public ElasticException(Throwable cause) {
        super(500, "ElasticException", cause.getMessage(), cause);
    }

    public ElasticException(String message) {
        super(500, "ElasticException", message);
    }

    public ElasticException(int code, Throwable cause) {
        super(code, "ElasticException", cause.getMessage(), cause);
    }

    public ElasticException(int code, String message) {
        super(code, "ElasticException", message);
    }

    public static StructuredException parse(Exception e) {
        if (e instanceof SocketTimeoutException) {
            return new TimeoutException(e);
        }
        return new ElasticException(e);
    }
}
