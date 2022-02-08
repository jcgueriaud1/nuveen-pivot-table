package org.vaadin.example.exceptionassignment.component;

import org.vaadin.example.exceptionassignment.model.Associate;

import java.util.Objects;

/**
 * @author Stefan Uebe
 */
public class AssociateItem implements GridItem {
    private final Associate wrapped;

    public AssociateItem(Associate wrapped) {
        this.wrapped = Objects.requireNonNull(wrapped);
    }

    public Associate getWrapped() {
        return wrapped;
    }

    @Override
    public String getAssociate() {
        return wrapped.getFullName();
    }

    @Override
    public Integer getCountByAssociate() {
        return wrapped.sumAllCountByRule();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssociateItem that = (AssociateItem) o;
        return getWrapped().equals(that.getWrapped());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getWrapped());
    }
}
