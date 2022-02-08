package org.vaadin.example.exceptionassignment.component;

import org.vaadin.example.exceptionassignment.model.AssignmentRule;

import java.util.Objects;

/**
 * @author Stefan Uebe
 */
public class RuleItem implements GridItem {
    private final AssignmentRule wrapped;

    public RuleItem(AssignmentRule wrapped) {
        this.wrapped = Objects.requireNonNull(wrapped);
    }

    public AssignmentRule getWrapped() {
        return wrapped;
    }

    @Override
    public String getAssignmentRule() {
        return wrapped.getAssignmentRule();
    }

    @Override
    public Integer getCountByRule() {
        return wrapped.getCountByRule();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RuleItem ruleItem = (RuleItem) o;
        return getWrapped().equals(ruleItem.getWrapped());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getWrapped());
    }
}
