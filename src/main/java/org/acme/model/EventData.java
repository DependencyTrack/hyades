package org.acme.model;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventData {
    private ArrayList<String> components;
    private String project;
    public int eventType;
}