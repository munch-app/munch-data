package munch.data.exceptions;

/**
 * Created by: Fuxing
 * Date: 12/12/2017
 * Time: 9:42 AM
 * Project: munch-data
 */
public class ClusterBlockException extends ElasticException {

    /**
     * http://docs.aws.amazon.com/elasticsearch-service/latest/developerguide/aes-handling-errors.html#aes-handling-errors-watermark
     */
    public ClusterBlockException() {
        super("cluster_block_exception");
    }
}
