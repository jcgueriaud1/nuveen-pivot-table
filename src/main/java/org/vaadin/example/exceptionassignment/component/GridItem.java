package org.vaadin.example.exceptionassignment.component;

import org.vaadin.example.exceptionassignment.model.AssignmentRule;

/**
 * @author Stefan Uebe
 */
public interface GridItem {
    default String getAssociate() {
        return ""; // must be "", otherwise it will show "null", since this is our hierarchical column
    }

    default Integer getCountByAssociate() {
        return null;
    }

    default String getAssignmentRule() {
        return null;
    }

    default Integer getCountByRule() {
        return null;
    }

    default AssociateItem asAssociate() {
        return (AssociateItem) this;
    }

    default RuleItem asRule() {
        return (RuleItem) this;
    }

    default boolean isAssociate() {
        return this instanceof AssociateItem;
    }

    default boolean isRule() {
        return this instanceof RuleItem;
    }

}
