package org.entitypedia.games.common.client;

/**
 * A common interface for all games clients.
 *
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface IGamesCommonClient {

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
     * @param signConnection connection signing flag
     */
    void setSignConnection(boolean signConnection);
}
