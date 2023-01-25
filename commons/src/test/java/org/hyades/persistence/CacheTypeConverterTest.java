package org.hyades.persistence;

import org.hyades.model.ComponentAnalysisCache;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CacheTypeConverterTest {

    @Test
    public void convertToDatastoreTest() {
        Assertions.assertNull((new CacheTypeConverter().convertToDatabaseColumn(null)));
        Assertions.assertTrue((new CacheTypeConverter().convertToDatabaseColumn(ComponentAnalysisCache.CacheType.REPOSITORY).equals("REPOSITORY")));
        Assertions.assertTrue((new CacheTypeConverter().convertToDatabaseColumn(ComponentAnalysisCache.CacheType.VULNERABILITY).equals("VULNERABILITY")));
    }

    @Test
    public void convertToAttributeTest() {
        Assertions.assertEquals(new CacheTypeConverter().convertToEntityAttribute("REPOSITORY"), ComponentAnalysisCache.CacheType.REPOSITORY);
        Assertions.assertEquals(new CacheTypeConverter().convertToEntityAttribute("VULNERABILITY"), ComponentAnalysisCache.CacheType.VULNERABILITY);

    }

}
