package org.vaadin.example.pivot.ui;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.function.SerializableRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vaadin.example.pivot.datasource.*;
import org.vaadin.example.pivot.datasource.PivotDataSource.PivotColumn;
import org.vaadin.example.pivot.datasource.PivotDataSource.PivotFilter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Allows the user to configure the {@link InMemoryPivot}. Provides a "+" button
 * by which the user can add the groupBy and aggregate clauses. Fires the {@link #pivotConfigurationChangedListeners}
 * when the user does so. You can use {@link #computePivotData(List)} to
 * configure the pivot easily with the new values.
 *
 * @author Martin Vysny <mavi@vaadin.com>
 */
public class PivotConfigurationPanel<T> extends Composite<Div> {

    private final List<GroupBy<T>> availableGroupByClauses = new ArrayList<>();
    private final List<PivotProperty<T>> availableAggregates = new ArrayList<>();

    @NotNull
    public final List<SerializableRunnable> pivotConfigurationChangedListeners = new ArrayList<>();
    @NotNull
    private final String allItemsCaption;
    @NotNull
    private final ItemsPane<PivotGroupByItem<T>> groupByPane = new ItemsPane<>(
            "Group by", "Choose group");
    @NotNull
    private final ItemsPane<PivotAggregateItem<T>> aggregatePane = new ItemsPane<>(
            "Values", "Choose value");


    private PivotFilter filter = new PivotFilter();

    @Override
    protected Div initContent() {
        final Div content = new Div();
        content.addClassName("pivot-configuration-panel");
        content.setWidth("450px");
        content.setMinWidth(content.getWidth());

        final HorizontalLayout caption = new HorizontalLayout();
        caption.setAlignItems(FlexComponent.Alignment.CENTER);
        caption.setWidthFull();
        caption.setHeight("40px");
        caption.addClassName("pivot-panel-caption");
        caption.addAndExpand(new Span("Pivot Settings"));
        final PivotIconButton closeButton = new PivotIconButton(VaadinIcon.CLOSE);
        closeButton.setColor("#444444");
        caption.add(closeButton);

        content.add(caption, groupByPane, aggregatePane);
        groupByPane.onItemListChanged = () -> {
            update();
            onPivotConfigurationChanged();
        };
        aggregatePane.onItemListChanged = this::onPivotConfigurationChanged;
        return content;
    }

    public PivotConfigurationPanel(@NotNull String allItemsCaption) {
        this.allItemsCaption = allItemsCaption;
        update();
    }

    /**
     * Registers a groupBy clause which the user can pick. A groupBy clause usually groups
     * on one particular property of the bean {@link T}.
     *
     * @param caption              the displayable caption of the group, for example 'Team Name'. Usually a field of the bean {@link T}.
     * @param groupedValueProvider retrieves the grouping value from the bean {@link T}; e.g. the <code>getTeamName()</code> getter.
     * @return this for fluent API.
     */
    @NotNull
    public PivotConfigurationPanel<T> addGroupBy(@NotNull String caption, @NotNull SerializableFunction<T, Object> groupedValueProvider) {
    	GroupBy<T> group = new GroupBy<>(new PivotProperty<>(caption, groupedValueProvider), false, false);
        availableGroupByClauses.add(group);
        update();
        return this;
    }

    /**
     * Registers an aggregate clause which the user can pick. An aggregate clause usually
     * aggregates (e.g. sums) on one particular property of the bean {@link T}.
     *
     * @param caption                 the displayable caption of the aggregate field, for example 'Age'. Usually a field of the bean {@link T}.
     * @param aggregatedValueProvider retrieves the grouping value from the bean {@link T}; e.g. the <code>getAge()</code> getter.
     * @return this for fluent API.
     */
    @NotNull
    public PivotConfigurationPanel<T> addAggregate(@NotNull String caption, @NotNull SerializableFunction<T, Number> aggregatedValueProvider) {
        availableAggregates.add(new PivotProperty<>(caption, aggregatedValueProvider));
        update();
        return this;
    }

    @Nullable
    private GroupBy<T> getTopGroup() {
        return getActiveGroupByClauses().stream()
                .filter(it -> it.columnGrouping)
                .findAny().orElse(null);
    }

    @NotNull
    private List<PivotGroupByItem<T>> getActiveGroupByItems() {
        return groupByPane.getItems();
    }

    @NotNull
    private List<PivotAggregateItem<T>> getActiveAggregateItems() {
        return aggregatePane.getItems();
    }

    private void setTopGroup(@Nullable PivotGroupByItem<T> newTopGroupItem) {
        for (PivotGroupByItem<T> item : getActiveGroupByItems()) {
            boolean topGroup = item.equals(newTopGroupItem);
            item.setTopGroup(topGroup);
            item.setFilterCheckboxEnabled(!topGroup);
        }
        onPivotConfigurationChanged();
    }

    public void setFirstAsDefault() {
    	GroupBy<T> group = availableGroupByClauses.get(0);
    	addActiveGroupBy(group);
    }

    private void addActiveGroupBy(@NotNull GroupBy<T> groupBy) {
        final PivotGroupByItem<T> item = new PivotGroupByItem<>(groupBy);
        item.addGroupByChangedListener(e -> {
            if (!e.isFilterEnabled()) {
                removeFilterValue(e.getGroupByClause().id);
            }
            if (item.isTopGroup()) {
                setTopGroup(item);
            } else {
                onPivotConfigurationChanged();
            }
        });
        item.addCloseListener(e -> {
            removeFilterValue(item.getGroupByClause().id);
            groupByPane.removeItem(item);
        });
        groupByPane.addItem(item);
    }

    private void addActiveAggregate(@NotNull Aggregate<T> aggregate, boolean isReadOnly) {
        final PivotAggregateItem<T> item = new PivotAggregateItem<>(aggregate, Arrays.asList(AggregateFunctionEnum.values()));
        item.setReadOnly(isReadOnly);
        item.addAggregateItemChangedListener(e -> {
            if (!e.isFilterEnabled()) {
                removeFilterValue(e.getAggregateClause().id);
            }
            onPivotConfigurationChanged();
        });
        item.addCloseListener(e -> {
            removeFilterValue(item.getAggregateClause().id);
            aggregatePane.removeItem(item);
        });
        aggregatePane.addItem(item);
    }

    private void update() {
        final Set<GroupBy<T>> activeGroupByClauses = new HashSet<>(getActiveGroupByClauses());
        final LinkedHashMap<String, SerializableRunnable> addGroupOptions = new LinkedHashMap<>();
        for (final GroupBy<T> allowedGroupByClause : availableGroupByClauses) {
            if (!activeGroupByClauses.contains(allowedGroupByClause)) {
                addGroupOptions.put(allowedGroupByClause.getCaption(), () -> addActiveGroupBy(allowedGroupByClause));
            }
        }
        groupByPane.setAddOptions(addGroupOptions);

        final LinkedHashMap<String, SerializableRunnable> addAggregateOptions = new LinkedHashMap<>();
        addAggregateOptions.put("Count", () -> {
            final Aggregate<T> aggregate = new Aggregate<>(new PivotProperty<>(allItemsCaption, SerializableFunction.identity()), AggregateFunctionEnum.COUNT, false);
            addActiveAggregate(aggregate, true);
        });
        for (PivotProperty<T> def : availableAggregates) {
            addAggregateOptions.put(def.caption, () -> addActiveAggregate(new Aggregate<>(def, AggregateFunctionEnum.SUM, false), false));
        }
        aggregatePane.setAddOptions(addAggregateOptions);
    }

    private void onPivotConfigurationChanged() {
        firePivotConfigurationChanged();
    }

    private void firePivotConfigurationChanged() {
        for (SerializableRunnable listener : pivotConfigurationChangedListeners) {
            listener.run();
        }
    }

    @NotNull
    public List<GroupBy<T>> getActiveGroupByClauses() {
        return getActiveGroupByItems().stream().map(PivotGroupByItem::getGroupByClause).collect(Collectors.toList());
    }

    @NotNull
    public List<Aggregate<T>> getActiveAggregateClauses() {
        return getActiveAggregateItems().stream().map(PivotAggregateItem::getAggregateClause).collect(Collectors.toList());
    }

    /**
     * Applies the currently configured groupBy and aggregate clauses to given pivot.
     *
     * @return the pivot to apply the configured groupBy and aggregate clauses to.
     */
    @NotNull
    public PivotDataSource.PivotResult<T> computePivotData(@NotNull List<T> items) {
        PivotDataSource<T> pivot = new ColumnGroupingPivotDataSource<>(new InMemoryPivot<>(), "Grand Total");

        final LinkedHashSet<GroupBy<T>> groupBySet = new LinkedHashSet<>(getActiveGroupByClauses());
        final LinkedHashSet<Aggregate<T>> aggregateSet = new LinkedHashSet<>(getActiveAggregateClauses());
        final Set<String> grandTotalsSet = aggregateSet.stream().map(it -> it.id).collect(Collectors.toSet());

        final PivotDataSource.PivotResult<T> result = pivot.computePivotRows(groupBySet, aggregateSet, grandTotalsSet, items, filter);
        result.setTopGroup(getTopGroup());
        return result;
    }

    private boolean hasColumnGroup() {
        return getTopGroup() != null;
    }

    /**
     * Removes all user-selected groupBy and aggregate configuration.
     */
    public void clear() {
        groupByPane.clear();
        aggregatePane.clear();
        filter = new PivotFilter();
    }

    public void setFilterValue(PivotColumn<?> column, Set<Object> value) {
        setFilterValue(column.id, value);
    }

    public void setFilterValue(String columnId, Set<Object> value) {
        Map<String, Set<Object>> filterValues = filter.getFilterValues();

        if (value != null) {
            filterValues.put(columnId, value);
        } else {
            filterValues.remove(columnId);
        }
    }

    public void removeFilterValue(PivotColumn<?> column) {
        removeFilterValue(column.id);
    }

    public void removeFilterValue(String columnId) {
        Map<String, Set<Object>> filterValues = filter.getFilterValues();
        filterValues.remove(columnId);
    }

    public void removeFilterValues() {
        filter.getFilterValues().clear();
    }

    public Set<Object> getFilterValue(PivotColumn<?> column) {
        return getFilterValue(column.id);
    }

    public Set<Object> getFilterValue(String columndId) {
        return filter.getFilterValues().get(columndId);
    }

    public Map<String, Set<Object>> getFilterValues() {
        return new HashMap<>(filter.getFilterValues());
    }
}
