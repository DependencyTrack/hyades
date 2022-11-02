package org.acme.persistence;

import alpine.model.Team;
import org.acme.model.Classifier;
import org.acme.model.Tag;

import java.util.*;

class ProjectQueryFilterBuilder {

    final private Map<String, Object> params;
    final private List<String> filterCriteria;

    ProjectQueryFilterBuilder() {
        this.params = new HashMap<>();
        this.filterCriteria = new ArrayList<>();
    }

    ProjectQueryFilterBuilder excludeInactive(boolean excludeInactive) {
        if (excludeInactive) {
            filterCriteria.add("(active == true || active == null)");
        }
        return this;
    }

    ProjectQueryFilterBuilder withTeam(Team team) {
        params.put("team", team);
        filterCriteria.add("(accessTeams.contains(:team))");
        return this;
    }

    ProjectQueryFilterBuilder withName(String name) {
        params.put("name", name);
        filterCriteria.add("(name == :name)");
        return this;
    }

    ProjectQueryFilterBuilder withVersion(String version) {
        params.put("version", version);
        filterCriteria.add("(version == :version)");
        return this;
    }

    ProjectQueryFilterBuilder withTag(Tag tag) {
        params.put("tag", tag);
        filterCriteria.add("(tags.contains(:tag))");
        return this;
    }

    ProjectQueryFilterBuilder withClassifier(Classifier classifier) {
        params.put("classifier", classifier);
        filterCriteria.add("(classifier == :classifier)");
        return this;
    }

    ProjectQueryFilterBuilder withFuzzyName(String name) {
        params.put("name", name);

        filterCriteria.add("(name.toLowerCase().matches(:name))");
        return this;
    }

    ProjectQueryFilterBuilder withFuzzyNameOrExactTag(String name, Tag tag) {
        params.put("name", name);
        params.put("tag", tag);

        filterCriteria.add("(name.toLowerCase().matches(:name) || tags.contains(:tag))");
        return this;
    }

    ProjectQueryFilterBuilder excludeProjectsThatAreChildren(){
        filterCriteria.add("parent == null");
        return this;
    }

    ProjectQueryFilterBuilder withParent(UUID uuid){
        params.put("parentUuid", uuid);

        filterCriteria.add("parent.uuid == :parentUuid");
        return this;
    }

    String buildFilter() {
        return String.join(" && ", this.filterCriteria);
    }

    Map<String, Object> getParams() {
        return params;
    }
}
