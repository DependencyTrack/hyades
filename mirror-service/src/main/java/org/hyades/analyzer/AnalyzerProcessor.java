package org.hyades.analyzer;

import org.apache.kafka.streams.processor.api.ContextualProcessor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.cyclonedx.model.Bom;
import org.hyades.model.Vulnerability;
import org.hyades.model.VulnerabilityScanKey;
import org.hyades.model.VulnerabilityScanResult;

public class AnalyzerProcessor extends ContextualProcessor<VulnerabilityScanKey, VulnerabilityScanResult, String, Bom> {

    public AnalyzerProcessor() {
    }

    @Override
    public void init(ProcessorContext<String, Bom> context) {
        super.init(context);
    }

    @Override
    public void process(Record<VulnerabilityScanKey, VulnerabilityScanResult> record) {

//        bovs.forEach(bov -> context().forward(record
//                .withKey(Vulnerability.Source.NVD.name() + "/" + bov.getVulnerabilities().get(0).getId())
//                .withValue(bov)));
    }
}
