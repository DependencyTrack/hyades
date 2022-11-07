package org.acme.common;

import javax.jdo.JDOUserException;

public interface AttributeConverter<A, D> {
    D convertToDatastore(A var1);

    A convertToAttribute(D var1);

    public static class UseDefault implements javax.jdo.AttributeConverter<Object, Object> {
        public UseDefault() {
        }

        public Object convertToDatastore(Object attributeValue) {
            throw new JDOUserException("This converter is not usable.");
        }

        public Object convertToAttribute(Object datastoreValue) {
            throw new JDOUserException("This converter is not usable.");
        }
    }
}
