package org.hyades.notification.model;

import org.apache.commons.lang3.StringUtils;
import org.hyades.notification.publisher.Publisher;

public enum PublisherClass {

    SlackPublisher,
    MsTeamsPublisher,
    MattermostPublisher,
    SendMailPublisher,
    ConsolePublisher,
    WebhookPublisher,
    CsWebexPublisher,
    JiraPublisher;

    public static Class<?> getPublisherClass(String publisherClassName) throws ClassNotFoundException {
        final var classPath = Publisher.class.getPackageName() + ".";
        if (StringUtils.containsIgnoreCase(publisherClassName, PublisherClass.SlackPublisher.name())){
            return Class.forName(classPath + PublisherClass.SlackPublisher.name());
        }
        if (StringUtils.containsIgnoreCase(publisherClassName, PublisherClass.MsTeamsPublisher.name())){
            return Class.forName(classPath + PublisherClass.MsTeamsPublisher.name());
        }
        if (StringUtils.containsIgnoreCase(publisherClassName, PublisherClass.MattermostPublisher.name())){
            return Class.forName(classPath + PublisherClass.MattermostPublisher.name());
        }
        if (StringUtils.containsIgnoreCase(publisherClassName, PublisherClass.SendMailPublisher.name())){
            return Class.forName(classPath + PublisherClass.SendMailPublisher.name());
        }
        if (StringUtils.containsIgnoreCase(publisherClassName, PublisherClass.ConsolePublisher.name())){
            return Class.forName(classPath + PublisherClass.ConsolePublisher.name());
        }
        if (StringUtils.containsIgnoreCase(publisherClassName, PublisherClass.WebhookPublisher.name())){
            return Class.forName(classPath + PublisherClass.WebhookPublisher.name());
        }
        if (StringUtils.containsIgnoreCase(publisherClassName, PublisherClass.CsWebexPublisher.name())){
            return Class.forName(classPath + PublisherClass.CsWebexPublisher.name());
        }
        if (StringUtils.containsIgnoreCase(publisherClassName, PublisherClass.JiraPublisher.name())){
            return Class.forName(classPath + PublisherClass.JiraPublisher.name());
        }
        return null;
    }
}
