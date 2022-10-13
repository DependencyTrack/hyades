package org.acme.consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.smallrye.common.annotation.Blocking;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import io.smallrye.reactive.messaging.kafka.KafkaRecordBatch;
import org.acme.event.SnykAnalysisEvent;
import org.acme.model.Component;
import org.acme.tasks.scanners.SnykAnalysisTask;
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

    @Incoming("EventNew")
    @Blocking
    public CompletionStage<Void> consume(KafkaRecordBatch<String, Component> records) {
            ArrayList<Component> componentArrayList = new ArrayList<>();
            for (KafkaRecord<String, Component> record : records) {
                Component payload = record.getPayload();
                component = payload;
                String topic = record.getTopic();
                logger.info("Printing topic name here: " + topic);
                logger.info(payload);
                logger.info("Printing payload purl here: " + payload.getPurl());
                logger.info(payload.getName());
                componentArrayList.add(component);
            }
            snykAnalysisEvent.setComponents(componentArrayList);

            snykTask.inform(snykAnalysisEvent);
            // ack will commit the latest offsets (per partition) of the batch.
            return records.ack();
    }
}
