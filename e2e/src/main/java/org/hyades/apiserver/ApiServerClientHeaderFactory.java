package org.hyades.apiserver;

import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

public class ApiServerClientHeaderFactory implements ClientHeadersFactory {

    public static String bearerToken;
    public static String apiKey;

    @Override
    public MultivaluedMap<String, String> update(final MultivaluedMap<String, String> incomingHeaders,
                                                 final MultivaluedMap<String, String> clientOutgoingHeaders) {
        final var headers = new MultivaluedHashMap<String, String>();
        if (apiKey != null) {
            headers.putSingle("X-Api-Key", apiKey);
        } else if (bearerToken != null) {
            headers.putSingle("Authorization ", "Bearer " + bearerToken);
        }
        return headers;
    }

    public static void reset() {
        bearerToken = null;
        apiKey = null;
    }

}
