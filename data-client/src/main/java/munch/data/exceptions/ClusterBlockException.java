package munch.data.exceptions;

/**
 * Created by: Fuxing
 * Date: 12/12/2017
 * Time: 9:42 AM
 * Project: munch-data
 */
public class ClusterBlockException extends ElasticException {

    public ClusterBlockException() {
        super("cluster_block_exception");
    }
}
