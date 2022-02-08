package org.vaadin.example.pivot.datasource;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * A grouping specifier, specifies how a list of beans of type {@link T} is grouped together.
 * All beans for a particular value retrieved via {@link #property} are grouped together.
 * @author Martin Vysny <mavi@vaadin.com>
 */
public final class GroupBy<T> implements Serializable {
    @NotNull
    public final String id;
    public final boolean columnGrouping;
    @NotNull
    public final PivotProperty<T> property;

    private final boolean filterEnabled;

    public GroupBy(@NotNull PivotProperty<T> property, boolean columnGrouping, boolean filterEnabled) {
        this(property, columnGrouping, property.caption + "/" + UUID.randomUUID(), filterEnabled);
    }

    public GroupBy(@NotNull PivotProperty<T> property, boolean columnGrouping, @NotNull String id, boolean filterEnabled) {
        this.property = Objects.requireNonNull(property);
        this.columnGrouping = columnGrouping;
        this.id = Objects.requireNonNull(id);
        this.filterEnabled = filterEnabled;
    }

    @NotNull
    public String getCaption() {
        return property.caption;
    }

    @NotNull
    public GroupBy<T> asColumnGroup() {
        return withColumnGroup(true);
    }

    @NotNull
    public GroupBy<T> withColumnGroup(boolean columnGroup) {
        return new GroupBy<>(property, columnGroup, id, !columnGroup && filterEnabled);
    }

    @Override
    public String toString() {
        return "GroupBy{" +
                "id='" + id + '\'' +
                ", columnGrouping=" + columnGrouping +
                ", property=" + property +
                '}';
    }

    /**
     * Returns the value of this group-by clause for given item.
     * @param item the bean
     * @return the value of {@link #property}.
     */
    @Nullable
    public Object getValue(@NotNull T item) {
        return property.getValue(item);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupBy<?> groupBy = (GroupBy<?>) o;
        return id.equals(groupBy.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }


    @NotNull
    public GroupBy<T> withFilterEnabled(boolean enabled) {
        return new GroupBy<>(property, columnGrouping, id, enabled);
    }

    public boolean isFilterEnabled() {
        return filterEnabled;
    }
}
