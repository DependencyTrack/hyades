package org.acme.consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import org.acme.common.ApplicationProperty;
import org.acme.event.SnykAnalysisEvent;
import org.acme.model.Component;
import org.acme.tasks.scanners.SnykAnalysisTask;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class SnykAnalyzer {
    Logger logger = Logger.getLogger("poc");

    @Inject
    SnykAnalysisTask snykTask;

    @Inject
    SnykAnalysisEvent snykAnalysisEvent;

    Component component;

    @Incoming("SnykEvent")
    public CompletionStage<Void> consume(KafkaRecord<String, Component> records) {
        Component payload = records.getPayload();
        component = payload;
        String topic = records.getTopic();
        logger.info("Printing topic name here: " + topic);
        logger.info(payload);
        logger.info("Printing payload purl here: " + payload.getPurl());
        logger.info(payload.getName());
        ArrayList<Component> componentArrayList = new ArrayList<>();
        componentArrayList.add(component);
        snykAnalysisEvent.setComponents(componentArrayList);

        snykTask.inform(snykAnalysisEvent);
        // ack will commit the latest offsets (per partition) of the batch.
        return records.ack();

    }

}