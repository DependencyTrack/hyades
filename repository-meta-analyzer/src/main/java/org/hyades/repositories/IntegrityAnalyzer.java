package org.hyades.repositories;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.hyades.model.IntegrityModel;
import org.hyades.persistence.model.Component;
import org.hyades.persistence.model.RepositoryType;
import org.hyades.proto.repointegrityanalysis.v1.HashMatchStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface that defines Integrity check analyzers.
 */
public interface IntegrityAnalyzer {

    Logger LOGGER = LoggerFactory.getLogger(IntegrityAnalyzer.class);

    RepositoryType supportedRepositoryType();

    String getName();

    default IntegrityModel getIntegrityModel(Component component) {
        return null;
    }

    void setHttpClient(final CloseableHttpClient httpClient);

    void setRepositoryBaseUrl(String baseUrl);

    void setRepositoryUsernameAndPassword(String username, String password);

    default IntegrityModel extractIntegrityModelFromResponse(Component component, CloseableHttpResponse response) {
        IntegrityModel integrityModel = new IntegrityModel();
        integrityModel.setComponent(component);
        try (response) {
            Header[] headers = response.getAllHeaders();
            String md5 = "";
            String sha1 = "";
            String sha256 = "";
            for (Header header : headers) {
                if (header.getName().equalsIgnoreCase("X-Checksum-MD5")) {
                    md5 = header.getValue();
                } else if (header.getName().equalsIgnoreCase("X-Checksum-SHA1")) {
                    sha1 = header.getValue();
                } else if (header.getName().equalsIgnoreCase("X-Checksum-SHA256")) {
                    sha256 = header.getValue();
                }
            }
            if (component.getMd5().isEmpty()) {
                integrityModel.setHashMatchStatusMd5(HashMatchStatus.HASH_MATCH_STATUS_COMPONENT_MISSING_HASH);
            }
            if (component.getSha1().isEmpty()) {
                integrityModel.setHashMatchStatusSha1(HashMatchStatus.HASH_MATCH_STATUS_COMPONENT_MISSING_HASH);
            }
            if (component.getSha256().isEmpty()) {
                integrityModel.setHashMatchStatusSha256(HashMatchStatus.HASH_MATCH_STATUS_COMPONENT_MISSING_HASH);
            }
            if (md5.equals("")) {
                integrityModel.setHashMatchStatusMd5(HashMatchStatus.HASH_MATCH_STATUS_UNKNOWN);
            }
            if (sha1.equals("")) {
                integrityModel.setHashMatchStatusSha1(HashMatchStatus.HASH_MATCH_STATUS_UNKNOWN);
            }
            if (sha256.equals("")) {
                integrityModel.setHashMatchStatusSha256(HashMatchStatus.HASH_MATCH_STATUS_UNKNOWN);
            }
            if (integrityModel.getHashMatchStatusMd5() == null) {
                //md5, sha1 or sha256 still "" means that the source of truth repo does not have this hash info and in that case, if there is a match with the others it is a valid component
                if (component.getMd5() != null && component.getMd5().equals(md5)) {
                    LOGGER.debug("Md5 hash matched: expected value :{}, actual value: {}", component.getMd5(), md5);
                    integrityModel.setHashMatchStatusMd5(HashMatchStatus.HASH_MATCH_STATUS_PASS);
                } else {
                    LOGGER.debug("Md5 hash did not match: expected value :{}, actual value: {}", component.getMd5(), md5);
                    integrityModel.setHashMatchStatusMd5(HashMatchStatus.HASH_MATCH_STATUS_FAIL);
                }
            }
            if (integrityModel.getHashMatchStatusSha1() == null) {
                if (component.getSha1() != null && component.getSha1().equals(sha1)) {
                    LOGGER.debug("sha1 hash matched: expected value: {}, actual value:{} ", component.getSha1(), sha1);
                    integrityModel.setHashMatchStatusSha1(HashMatchStatus.HASH_MATCH_STATUS_PASS);
                } else {
                    LOGGER.debug("sha1 hash did not match: expected value :{}, actual value: {}", component.getSha1(), sha1);
                    integrityModel.setHashMatchStatusSha1(HashMatchStatus.HASH_MATCH_STATUS_FAIL);
                }
            }
            if (integrityModel.getHashMatchStatusSha256() == null) {
                if (component.getSha256() != null && component.getSha256().equals(sha256)) {
                    LOGGER.debug("sha256 hash matched: expected value: {}, actual value:{}", component.getSha256(), sha256);
                    integrityModel.setHashMatchStatusSha256(HashMatchStatus.HASH_MATCH_STATUS_PASS);
                } else {
                    LOGGER.debug("sha256 hash did not match: expected value :{}, actual value: {}", component.getSha256(), sha256);
                    integrityModel.setHashMatchStatusSha256(HashMatchStatus.HASH_MATCH_STATUS_FAIL);
                }
            }
        } catch (Exception ex) {
            LOGGER.error("An error occurred while performing head request for component: " + ex);
        }
        return integrityModel;
    }
}
