package org.acme.persistence;

import org.acme.model.Severity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SeverityConverterTest {

    @Test
    public void testConvertToDatabaseColumn() {
        Assertions.assertEquals(new SeverityConverter().convertToDatabaseColumn(Severity.HIGH), Severity.HIGH.name());
        Assertions.assertEquals(new SeverityConverter().convertToDatabaseColumn(null), null);
    }

    @Test
    public void testConvertToEntityAttribute() {
        Assertions.assertEquals(new SeverityConverter().convertToEntityAttribute("CRITICAL"), Severity.CRITICAL);
        Assertions.assertEquals(new SeverityConverter().convertToEntityAttribute(null), null);
    }
}
