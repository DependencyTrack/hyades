package org.hyades.apiserver;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

public class ApiServerClientHeaderFactory implements ClientHeadersFactory {

    private static String bearerToken;
    private static String apiKey;

    @Override
    public MultivaluedMap<String, String> update(final MultivaluedMap<String, String> incomingHeaders,
                                                 final MultivaluedMap<String, String> clientOutgoingHeaders) {
        final var headers = new MultivaluedHashMap<String, String>();
        if (apiKey != null) {
            headers.putSingle("X-Api-Key", apiKey);
        } else if (bearerToken != null) {
            headers.putSingle("Authorization", "Bearer " + bearerToken);
        }
        return headers;
    }

    public static void setBearerToken(final String bearerToken) {
        ApiServerClientHeaderFactory.bearerToken = bearerToken;
    }

    public static void setApiKey(final String apiKey) {
        ApiServerClientHeaderFactory.apiKey = apiKey;
    }

    public static void reset() {
        ApiServerClientHeaderFactory.bearerToken = null;
        ApiServerClientHeaderFactory.apiKey = null;
    }

}
