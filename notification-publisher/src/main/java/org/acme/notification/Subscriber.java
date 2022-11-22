package org.acme.notification;

import org.acme.model.Notification;

public interface Subscriber {
    void inform(Notification var1);
}
