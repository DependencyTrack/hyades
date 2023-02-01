package org.hyades.nvd;

import io.quarkus.runtime.annotations.RegisterForReflection;

@SuppressWarnings("unused")
@RegisterForReflection(targets = {
        io.github.jeremylong.nvdlib.nvd.Config.class,
        io.github.jeremylong.nvdlib.nvd.CpeMatch.class,
        io.github.jeremylong.nvdlib.nvd.CveApiJson20.class,
        io.github.jeremylong.nvdlib.nvd.CveItem.class,
        io.github.jeremylong.nvdlib.nvd.CvssV2.class,
        io.github.jeremylong.nvdlib.nvd.CvssV20.class,
        io.github.jeremylong.nvdlib.nvd.CvssV30.class,
        io.github.jeremylong.nvdlib.nvd.CvssV30__1.class,
        io.github.jeremylong.nvdlib.nvd.CvssV31.class,
        io.github.jeremylong.nvdlib.nvd.CvssV31__1.class,
        io.github.jeremylong.nvdlib.nvd.DefCveItem.class,
        io.github.jeremylong.nvdlib.nvd.LangString.class,
        io.github.jeremylong.nvdlib.nvd.Metrics.class,
        io.github.jeremylong.nvdlib.nvd.Node.class,
        io.github.jeremylong.nvdlib.nvd.Reference.class,
        io.github.jeremylong.nvdlib.nvd.VendorComment.class,
        io.github.jeremylong.nvdlib.nvd.Weakness.class
}, ignoreNested = false)
public class NvdReflectionConfiguration {
}
