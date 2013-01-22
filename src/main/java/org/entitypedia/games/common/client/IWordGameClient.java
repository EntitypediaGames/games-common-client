package org.entitypedia.games.common.client;

/**
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface IWordGameClient {

    /**
     * Returns current api endpoint.
     * @return current api endpoint
     */
    String getApiEndpoint();

    /**
     * Sets api endpoint.
     * @param apiEndpoint api endpoint
     */
    void setApiEndpoint(String apiEndpoint);

    /**
     * Returns connection signing flag.
     * @return connection signing flag
     */
    boolean getSignConnection();

    /**
     * Sets connection signing flag. If true all API calls will be signed.
     * @param signConnection
     */
    void setSignConnection(boolean signConnection);
}
