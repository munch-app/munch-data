package munch.data.resolver;

/**
 * Created by: Fuxing
 * Date: 22/8/18
 * Time: 5:58 PM
 * Project: munch-data
 */
public final class ResolverHaltException extends RuntimeException {

    /**
     * Halt creation of Place
     *
     * @param message message for stopping creation of place
     */
    public ResolverHaltException(String message) {
        super(message);
    }
}
