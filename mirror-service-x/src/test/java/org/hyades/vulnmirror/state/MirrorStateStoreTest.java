package org.hyades.vulnmirror.state;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.kafka.common.errors.SerializationException;
import org.hyades.vulnmirror.datasource.Datasource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@QuarkusTest
class MirrorStateStoreTest {

    @Inject
    MirrorStateStore stateStore;

    @AfterEach
    void afterEach() {
        stateStore.clear();
    }

    @Test
    void test() {
        final ObjectNode nvdState = JsonNodeFactory.instance.objectNode();
        nvdState.put("foo", "bar");
        nvdState.put("baz", 12345);

        final String githubState = "foobarbaz";

        stateStore.putAndWait(Datasource.NVD, nvdState);
        stateStore.putAndWait(Datasource.GITHUB, githubState);

        final ObjectNode savedNvdState = stateStore.get(Datasource.NVD, ObjectNode.class);
        assertThat(savedNvdState).isNotNull();
        assertThat(savedNvdState.get("foo").asText()).isEqualTo("bar");
        assertThat(savedNvdState.get("baz").asInt()).isEqualTo(12345);

        final String savedGithubState = stateStore.get(Datasource.GITHUB, String.class);
        assertThat(savedGithubState).isEqualTo("foobarbaz");
    }

    @Test
    void testGet() {
        final ObjectNode savedState = stateStore.get(Datasource.NVD, ObjectNode.class);
        assertThat(savedState).isNull();
    }

    @Test
    void testGetWithDeserializationError() {
        final ObjectNode state = JsonNodeFactory.instance.objectNode();
        state.put("foo", "bar");
        state.put("baz", 12345);

        stateStore.putAndWait(Datasource.NVD, state);

        assertThatExceptionOfType(SerializationException.class)
                .isThrownBy(() -> stateStore.get(Datasource.NVD, ArrayNode.class));
    }

}