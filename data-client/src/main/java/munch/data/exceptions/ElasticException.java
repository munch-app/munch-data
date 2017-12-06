package munch.data.exceptions;

import munch.restful.core.exception.StructuredException;
import munch.restful.core.exception.TimeoutException;

import java.net.SocketTimeoutException;

/**
 * Created by: Fuxing
 * Date: 10/10/2017
 * Time: 11:15 AM
 * Project: munch-data
 */
public class ElasticException extends StructuredException {

    public ElasticException(Throwable cause) {
        super(500, "ElasticException", cause.getMessage(), cause);
    }

    public static StructuredException parse(Exception e) {
        if (e instanceof SocketTimeoutException) {
            return new TimeoutException(e);
        }
        return new ElasticException(e);
    }
}
