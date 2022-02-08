package org.vaadin.example.pivot.ui;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableSupplier;
import org.apache.commons.lang3.BooleanUtils;
import org.jetbrains.annotations.NotNull;
import org.vaadin.example.pivot.datasource.Aggregate;
import org.vaadin.example.pivot.datasource.AggregateFunction;

import java.util.List;

/**
 * @author Martin Vysny <mavi@vaadin.com>
 */
public class PivotAggregateItem<T> extends Composite<PivotConfigurationItem> {
    @NotNull
    private Aggregate<T> aggregate;
    @NotNull
    private final Select<AggregateFunction> aggregateFunctionComboBox;

    private final HorizontalLayout container;
    private final Checkbox filterCheckbox;

    public PivotAggregateItem(@NotNull Aggregate<T> aggregate, @NotNull List<AggregateFunction> aggregateFunctions) {
        container = new HorizontalLayout();
        container.setAlignItems(FlexComponent.Alignment.CENTER);

        this.aggregate = aggregate;
        aggregateFunctionComboBox = new Select<>();
        aggregateFunctionComboBox.setItems(aggregateFunctions);
        aggregateFunctionComboBox.setItemLabelGenerator(AggregateFunction::getCaption);
        aggregateFunctionComboBox.getElement().getThemeList().add("small");
        aggregateFunctionComboBox.setValue(aggregate.function);
        aggregateFunctionComboBox.setWidth("120px");
        aggregateFunctionComboBox.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                setAggregateFunction(e.getValue());
            }
        });

        filterCheckbox = new Checkbox("Filter", aggregate.isFilterEnabled());
        filterCheckbox.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                setFilterEnabled(BooleanUtils.toBoolean(event.getValue()));
            }
        });

        container.add(aggregateFunctionComboBox, filterCheckbox);
    }

    @Override
    protected PivotConfigurationItem initContent() {
        final PivotConfigurationItem item = new PivotConfigurationItem(aggregate.property.caption);
        item.setSlotComponent(container);
        return item;
    }

    @NotNull
    public Aggregate<T> getAggregateClause() {
        return aggregate;
    }

    @NotNull
    public AggregateFunction getAggregateFunction() {
        return aggregate.function;
    }


    public void setAggregateFunction(@NotNull AggregateFunction aggregateFunction) {
        if (getAggregateFunction() != aggregateFunction) {
            aggregate = aggregate.withFunction(aggregateFunction);
            aggregateFunctionComboBox.setValue(aggregateFunction);
        }
    }


    /**
     * Allows filtering for the column represented by this item. When enabled, it will result in a filter field
     * shown in the header of the respective column. Does not affect the filter checkbox.
     * @param enabled enable filtering for this item's column
     */
    public void setFilterEnabled(boolean enabled) {
        if (isFilterEnabled() != enabled) {
            aggregate = aggregate.withFilterEnabled(enabled);
            filterCheckbox.setValue(enabled);
        }
    }

    /**
     * Indicates, if filtering is enabled for the column represented by this item. Does not indicate, if the
     * filter checkbox itself might be enabled or not.
     * @return filtering is allowed / enabled for this item's column
     */
    public boolean isFilterEnabled() {
        return aggregate.isFilterEnabled();
    }

    public void addAggregateItemChangedListener(@NotNull SerializableConsumer<PivotAggregateItem<T>> listener) {
        aggregateFunctionComboBox.addValueChangeListener(event -> listener.accept(this));
        filterCheckbox.addValueChangeListener(event -> listener.accept(this));
    }

    public void addCloseListener(@NotNull ComponentEventListener<ClickEvent<Icon>> e) {
        getContent().addCloseListener(e);
    }

    public void setReadOnly(boolean isReadOnly) {
        aggregateFunctionComboBox.setEnabled(!isReadOnly);
    }

    @Override
    public String toString() {
        return "PivotAggregateItem{" + aggregate + "}";
    }

}
