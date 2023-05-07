package org.hyades.persistence.model;

public enum NotificationLevel {
    
        INFORMATIONAL(2),
        WARNING(1),
        ERROR(0);

        private int level;

        private NotificationLevel(int level) {
            this.level = level;
        }

        public int asNumericLevel() {
            return this.level;
        }

}
