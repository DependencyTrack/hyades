package org.acme.model;

import java.time.LocalDateTime;

public class Notification {

        private String scope;
        private String group;
        private NotificationLevel level;
        private String title;
        private String content;
        private LocalDateTime timestamp = LocalDateTime.now();
        private Object subject;

        public static void dispatch(Notification notification) {
            // TODO @NotificationService
        }

        public Notification() {
        }

        public Notification scope(String scope) {
            this.scope = scope;
            return this;
        }

        public Notification scope(Enum scope) {
            this.scope = scope.name();
            return this;
        }

        public Notification group(String group) {
            this.group = group;
            return this;
        }

        public Notification group(Enum group) {
            this.group = group.name();
            return this;
        }

        public Notification level(NotificationLevel level) {
            this.level = level;
            return this;
        }

        public Notification title(String title) {
            this.title = title;
            return this;
        }

        public Notification title(Enum title) {
            this.title = title.name();
            return this;
        }

        public Notification content(String content) {
            this.content = content;
            return this;
        }

        public Notification content(Enum content) {
            this.content = content.name();
            return this;
        }

        public Notification timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Notification subject(Object subject) {
            this.subject = subject;
            return this;
        }

        public String getScope() {
            return this.scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }

        public String getGroup() {
            return this.group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public NotificationLevel getLevel() {
            return this.level;
        }

        public void setLevel(NotificationLevel level) {
            this.level = level;
        }

        public String getTitle() {
            return this.title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return this.content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public LocalDateTime getTimestamp() {
            return this.timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public Object getSubject() {
            return this.subject;
        }

        public void setSubject(Object subject) {
            this.subject = subject;
        }

}
