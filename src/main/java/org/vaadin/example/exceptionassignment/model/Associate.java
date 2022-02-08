package org.vaadin.example.exceptionassignment.model;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Stefan Uebe
 */
public class Associate implements Serializable {
    private final long id;
    private String fullName;
    private List<AssignmentRule> rules;

    public Associate(long id, String fullName) {
        this.id = id;
        this.fullName = fullName;
    }

    /**
     * Returns a sum of the assignment rule
     * @return sum
     */
    public int sumAllCountByRule() {
        return streamRules().mapToInt(AssignmentRule::getCountByRule).sum();
    }

    @NotNull
    public Stream<AssignmentRule> streamRules() {
        return rules != null ? rules.stream() : Stream.empty();
    }

    public List<AssignmentRule> getRules() {
        return Collections.unmodifiableList(rules);
    }

    /**
     * Used for internal data modification.
     * @return unchanged list
     */
    List<AssignmentRule> getRulesToModify() {
        return rules;
    }

    public void setRules(List<AssignmentRule> rules) {
        this.rules = rules;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Associate associate = (Associate) o;
        return getId() == associate.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
