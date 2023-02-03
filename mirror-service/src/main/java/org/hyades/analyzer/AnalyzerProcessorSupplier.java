package org.hyades.analyzer;

import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.cyclonedx.model.Bom;
import org.hyades.model.VulnerabilityScanKey;
import org.hyades.model.VulnerabilityScanResult;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class AnalyzerProcessorSupplier implements ProcessorSupplier<VulnerabilityScanKey, VulnerabilityScanResult, String, Bom> {

    @Inject
    public AnalyzerProcessorSupplier() {
    }

    @Override
    public Processor<VulnerabilityScanKey, VulnerabilityScanResult, String, Bom> get() {
        return new AnalyzerProcessor();
    }
}
