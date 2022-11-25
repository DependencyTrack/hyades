package org.acme.persistence;

import io.quarkus.test.junit.QuarkusTest;
import org.acme.model.ComponentAnalysisCache;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


@QuarkusTest
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
