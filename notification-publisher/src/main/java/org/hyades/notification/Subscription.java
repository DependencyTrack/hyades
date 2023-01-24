//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.hyades.notification;

import org.hyades.model.NotificationLevel;

public class Subscription {
    private final Class<? extends Subscriber> subscriber;
    private String scope;
    private String group;
    private NotificationLevel level;

    public Subscription(Class<? extends Subscriber> subscriber) {
        this.subscriber = subscriber;
    }

    public Subscription(Class<? extends Subscriber> subscriber, String group) {
        this.subscriber = subscriber;
        this.group = group;
    }

    public Subscription(Class<? extends Subscriber> subscriber, NotificationLevel level) {
        this.subscriber = subscriber;
        this.level = level;
    }

    public Subscription(Class<? extends Subscriber> subscriber, String scope, String group, NotificationLevel level) {
        this.subscriber = subscriber;
        this.scope = scope;
        this.group = group;
        this.level = level;
    }

    public Class<? extends Subscriber> getSubscriber() {
        return this.subscriber;
    }

    public String getScope() {
        return this.scope;
    }

    public String getGroup() {
        return this.group;
    }

    public NotificationLevel getLevel() {
        return this.level;
    }

    public boolean equals(Object object) {
        if (object instanceof Subscription subscription) {
            if (subscription.getScope() != null && !subscription.getScope().equals(this.scope)) {
                return false;
            } else if (this.getScope() != null && !subscription.getScope().equals(this.scope)) {
                return false;
            } else if (subscription.getGroup() != null && !subscription.getGroup().equals(this.group)) {
                return false;
            } else if (this.getGroup() != null && !subscription.getGroup().equals(this.group)) {
                return false;
            } else if (subscription.getLevel() != null && subscription.getLevel() != this.level) {
                return false;
            } else if (this.getLevel() != null && subscription.getLevel() != this.level) {
                return false;
            } else {
                return subscription.getSubscriber() == this.subscriber;
            }
        } else {
            return false;
        }
    }
}
