package org.hyades.nvd;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Register classes of {@code nvd-lib} for reflection.
 * <p>
 * Because the NVD library uses Jackson for deserialization, reflective access
 * to model classes is required. It's not possible to register entire packages,
 * so we're left with having to register every single class by hand. Fun!
 * <p>
 * The model classes are generated from JSON in {@code nvd-lib}.
 * When inspecting the library's code on GitHub, you won't find these classes.
 */
@SuppressWarnings("unused")
@RegisterForReflection(
        targets = {
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
        },
        ignoreNested = false
)
public class NvdReflectionConfiguration {
}
