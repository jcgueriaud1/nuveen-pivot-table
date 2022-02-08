package org.vaadin.example.exceptionassignment.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Stefan Uebe
 */
public class AssignmentRule implements Serializable {
    private final long id;
    private Associate parent;
    private String assignmentRule;
    private int countByRule;

    public AssignmentRule(long id, Associate parent, String assignmentRule, int countByRule) {
        this.id = id;
        this.parent = parent;
        this.assignmentRule = assignmentRule;
        this.countByRule = countByRule;
    }

    public String getAssignmentRule() {
        return assignmentRule;
    }

    public void setAssignmentRule(String assignmentRule) {
        this.assignmentRule = assignmentRule;
    }

    public int getCountByRule() {
        return countByRule;
    }

    public void setCountByRule(int countByRule) {
        this.countByRule = countByRule;
    }

    public Associate getParent() {
        return parent;
    }

    public void setParent(Associate parent) {
        this.parent = parent;
    }

    public long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssignmentRule that = (AssignmentRule) o;
        return getId() == that.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
