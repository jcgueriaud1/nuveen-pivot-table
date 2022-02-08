package org.vaadin.example.pivot.ui;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.function.SerializableConsumer;
import org.apache.commons.lang3.BooleanUtils;
import org.jetbrains.annotations.NotNull;
import org.vaadin.example.pivot.datasource.GroupBy;

/**
 * @author Martin Vysny <mavi@vaadin.com>
 */
public class PivotGroupByItem<T> extends Composite<PivotConfigurationItem> {
    @NotNull
    private GroupBy<T> groupBy;
    @NotNull
    private final Checkbox topGroup;

    private final HorizontalLayout container;
    private final Checkbox filterCheckbox;

    public PivotGroupByItem(@NotNull GroupBy<T> groupBy) {
        container = new HorizontalLayout();
        container.setAlignItems(FlexComponent.Alignment.CENTER);

        this.groupBy = groupBy;
        topGroup = new Checkbox("Top group", groupBy.columnGrouping);
        filterCheckbox = new Checkbox("Filter", groupBy.isFilterEnabled());

        topGroup.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                boolean value = BooleanUtils.toBoolean(e.getValue());
                setTopGroup(value);
            }
        });

        filterCheckbox.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                setFilterEnabled(BooleanUtils.toBoolean(event.getValue()));
            }
        });

        container.add(topGroup, filterCheckbox);
    }

    @Override
    protected PivotConfigurationItem initContent() {
        final PivotConfigurationItem item = new PivotConfigurationItem(groupBy.getCaption());
        item.setSlotComponent(container);
        return item;
    }

    @NotNull
    public GroupBy<T> getGroupByClause() {
        return groupBy;
    }

    public boolean isTopGroup() {
        return groupBy.columnGrouping;
    }

    public void setTopGroup(boolean isTopGroup) {
        if (isTopGroup() != isTopGroup) {
            groupBy = groupBy.withColumnGroup(isTopGroup);
            topGroup.setValue(isTopGroup);
        }
    }

    /**
     * This method enables the filter checkbox of this item. It is for instance necessary, when this item
     * is set as top group. Will also uncheck it, when disabled.
     * @param enabled enable the checkbox
     */
    void setFilterCheckboxEnabled(boolean enabled) {
        filterCheckbox.setValue(enabled && BooleanUtils.toBoolean(filterCheckbox.getValue()));
        filterCheckbox.setEnabled(enabled);
    }

    /**
     * Allows filtering for the column represented by this item. When enabled, it will result in a filter field
     * shown in the header of the respective column. Has not visible effect for the top group and does not affect
     * the filter checkbox.
     * @param enabled enable filtering for this item's column
     */
    public void setFilterEnabled(boolean enabled) {
        if (isFilterEnabled() != enabled) {
            groupBy = groupBy.withFilterEnabled(enabled);
            filterCheckbox.setValue(enabled);
        }
    }

    /**
     * Indicates, if filtering is enabled for the column represented by this item. Does not indicate, if the
     * filter checkbox itself might be enabled or not.
     * @return filtering is allowed / enabled for this item's column
     */
    public boolean isFilterEnabled() {
        return groupBy.isFilterEnabled();
    }

    public void addGroupByChangedListener(@NotNull SerializableConsumer<PivotGroupByItem<T>> listener) {
        topGroup.addValueChangeListener(event -> listener.accept(this));
        filterCheckbox.addValueChangeListener(event -> listener.accept(this));
    }

    public void addCloseListener(@NotNull ComponentEventListener<ClickEvent<Icon>> e) {
        getContent().addCloseListener(e);
    }

    @Override
    public String toString() {
        return "PivotGroupByItem{" + groupBy + "}";
    }
}
