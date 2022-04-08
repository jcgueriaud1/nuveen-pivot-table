package org.vaadin.example;

import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.FooterRow;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.jetbrains.annotations.NotNull;
import org.vaadin.example.pivot.datasource.PivotDataSource;
import org.vaadin.example.pivot.datasource.PivotDataSource.Row;
import org.vaadin.example.pivot.ui.PivotConfigurationPanel;
import org.vaadin.gatanaso.MultiselectComboBox;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The main view contains a button and a click listener.
 */
@Route("filter")
public class PlayerPivotExternalFilterView extends VerticalLayout {

	public PlayerPivotExternalFilterView() {
		final Grid<Player> playerGrid = new Grid<>();
		playerGrid.addColumn(Player::getName).setHeader("Name").setSortable(true);
		playerGrid.addColumn(Player::getTeam).setHeader("Team").setSortable(true);
		playerGrid.addColumn(Player::getPosition).setHeader("Position").setSortable(true);
		playerGrid.addColumn(Player::getHeightInches).setHeader("Height (inches)").setSortable(true);
		playerGrid.addColumn(Player::getWeightLbs).setHeader("Weight (lbs)").setSortable(true);
		playerGrid.addColumn(Player::getAge).setHeader("Age").setSortable(true);
		playerGrid.setItems(Player.ALL_PLAYERS);
		add(playerGrid);

		add(new Button("Show Pivot Table", e -> {
			e.getSource().setVisible(false);
			playerGrid.getElement().removeFromParent();
			showPivotTable();
		}));
		filterComponent = new VerticalLayout();
		filterComponent.setSpacing(false);
		filterComponent.setPadding(false);
//        showPivotTable();
	}

	private Grid<Row<Player>> pivotTable;
	private VerticalLayout filterComponent;

	private void showPivotTable() {
		final HorizontalLayout bar = new HorizontalLayout();

		final PivotConfigurationPanel<Player> pivotConfigurationPanel = new PivotConfigurationPanel<Player>("Players")
				.addGroupBy("Team", Player::getTeam).addGroupBy("Position", Player::getPosition)
				.addAggregate("Height (inch)", Player::getHeightInches)
				.addAggregate("Weight (lbs)", Player::getWeightLbs).addAggregate("Age", Player::getAge);

		pivotTable = createPivotTable(pivotConfigurationPanel, filterComponent);
		pivotConfigurationPanel.pivotConfigurationChangedListeners.add(() -> {
			Notification.show("Pivot updated");
			List<GridSortOrder<Row<Player>>> sort = pivotTable.getSortOrder();
			final Grid<Row<Player>> newPivotTable = createPivotTable(pivotConfigurationPanel, filterComponent);
			// Copy sort orders of the old pivot table to a new one
			List<GridSortOrder<Row<Player>>> newSort = copySort(sort, newPivotTable);
			bar.replace(pivotTable, newPivotTable);
			pivotTable = newPivotTable;
			pivotTable.sort(newSort);
		});

		pivotConfigurationPanel.setFirstAsDefault();

		bar.setWidthFull();
		bar.addAndExpand(pivotTable);
		bar.add(pivotConfigurationPanel);
		bar.setSpacing(false);
		add(filterComponent);
		add(bar);
	}

	private List<GridSortOrder<Row<Player>>> copySort(List<GridSortOrder<Row<Player>>> sort,
			final Grid<Row<Player>> newPivotTable) {
		List<GridSortOrder<Row<Player>>> newSort = new ArrayList<>();
		sort.forEach(s -> {
			String key = s.getSorted().getKey();
			// Since column instance is the key, we cannot simply use old sort order as is,
			// but we
			// need to create new copy. This works only if Grid columns have key assigned.
			GridSortOrder<Row<Player>> so = new GridSortOrder<Row<Player>>(newPivotTable.getColumnByKey(key),
					s.getDirection());
			newSort.add(so);
		});
		return newSort;
	}

	@NotNull
	private Grid<Row<Player>> createPivotTable(@NotNull PivotConfigurationPanel<Player> panel,
											   HasComponents filterComponent) {
		return createPivotTable(panel, Player.ALL_PLAYERS, filterComponent);
	}

	@NotNull
	public static <T> Grid<Row<T>> createPivotTable(@NotNull PivotConfigurationPanel<T> panel,
                                                    @NotNull List<T> items,
													HasComponents filterComponent) {
		final Grid<Row<T>> pivotTable = new Grid<>();
		pivotTable.setWidthFull();
		pivotTable.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
		HeaderRow defaultHeaderRow = pivotTable.appendHeaderRow();

		final PivotDataSource.PivotResult<T> pivotResult = panel.computePivotData(items);
		pivotTable.setItems(pivotResult.rows);

		for (PivotDataSource.PivotColumn<T> column : pivotResult.columns) {
			final Column<Row<T>> col = pivotTable.addColumn(it -> it.get(column.id))
//                    .setFlexGrow(1)
					.setHeader(column.getCaption())
//                    .setAutoWidth(true) // see https://github.com/vaadin/vaadin-grid/issues/2174
					.setSortable(true);
			col.setKey(column.id);
		}

		if (pivotResult.hasColumnGrouping()) {
			HeaderRow groupingHeader = pivotTable.prependHeaderRow();
			final Map<Object, List<Column<Row<T>>>> columnGroups = pivotTable.getColumns().stream()
					.filter(col -> pivotResult.getColumn(col.getKey()).columnGroupValue != null)
					.collect(Collectors.groupingBy(col -> pivotResult.getColumn(col.getKey()).columnGroupValue));

			for (Map.Entry<Object, List<Column<Row<T>>>> entry : columnGroups.entrySet()) {
				final String columnGroupValue = entry.getKey().toString();
				if (entry.getValue().size() == 1) {
					groupingHeader.getCell(entry.getValue().get(0)).setText(columnGroupValue);
				} else {
					groupingHeader.join(entry.getValue().toArray(new Column[0])).setText(columnGroupValue);
				}
			}
		}

		if (pivotResult.hasEnabledFilters()) {
			updateExternalFilters(filterComponent, panel, items, pivotTable, pivotResult);
		}

		updateGrandTotalFooter(pivotTable, pivotResult);

		pivotTable.setSelectionMode(SelectionMode.NONE);

		pivotTable.addItemClickListener(event -> {
			String key = event.getColumn().getKey();
			int col = pivotTable.getColumns().indexOf(pivotTable.getColumnByKey(key)) + 1;
			Object value = event.getItem().get(key);
			int row = pivotResult.rows.indexOf(event.getItem()) + 1;
			// Push the focus to the cell clicked
			// It will get the focus, but un-fortunately focus ring will not show up
			// If you continue the navigation with cursor keys, the focus ring will appear
			pivotTable.getElement().executeJs(
					"window.requestAnimationFrame(function(){let firstTd = $0.shadowRoot.querySelector('tr:nth-child(" + row 
							+ ") > td:nth-child(" + col + ")'); firstTd.focus(); })",
					pivotTable.getElement());
			Notification.show("Cell (" + row + "," + col + ") clicked. Cell value is " + value);
		});
		return pivotTable;
	}


	private static <T> void updateExternalFilters(HasComponents filterComponent, @NotNull PivotConfigurationPanel<T> panel,
												  @NotNull List<T> items, Grid<Row<T>> pivotTable,
												  PivotDataSource.PivotResult<T> pivotResult) {
		filterComponent.removeAll();
		for (Column<Row<T>> column : pivotTable.getColumns()) {
			PivotDataSource.PivotColumn<T> pivotColumn = pivotResult.getColumn(column.getKey());
			if (pivotColumn.isFilterEnabled()) {
				MultiselectComboBox<Object> filterField = new MultiselectComboBox<>(); // replace with select and a button?
				filterField.setClearButtonVisible(true);
				filterField.setWidthFull();
				filterField.setMinWidth("75px");

				filterField.setLabel(pivotResult.getColumn(column.getKey()).getCaption());
				filterComponent.add(filterField);

				Set<Object> objectSet = pivotResult.rows.stream().map(row -> row.get(column.getKey()))
						.filter(Objects::nonNull).collect(Collectors.toSet());

				List<Object> objectList = new ArrayList<>(objectSet);
				if (!objectList.isEmpty()) {
					if (objectList.get(0) instanceof Comparable) { // assuming for now that all items of that column are
						// of the same class
						objectList.sort(Comparator.comparing(o -> (Comparable<Object>) o));
					} else {
						objectList.sort(Comparator.comparing(Object::toString));
					}
				}

				filterField.setItems(objectList);
				filterField.setValue(panel.getFilterValue(pivotColumn));

				filterField.addValueChangeListener(event -> {
					if (event.isFromClient()) {
						panel.setFilterValue(pivotColumn, event.getValue());

						PivotDataSource.PivotResult<T> filteredPivotResult = panel.computePivotData(items);
						pivotTable.setItems(filteredPivotResult.rows);

						updateExternalFilters(filterComponent, panel, items, pivotTable, pivotResult);
						updateGrandTotalFooter(pivotTable, filteredPivotResult);
					}
				});
			}
		}
	}

	private static <T> void updateGrandTotalFooter(Grid<Row<T>> pivotTable,
			PivotDataSource.PivotResult<T> pivotResult) {
		if (pivotResult.hasGrandTotals()) {
			List<FooterRow> footerRows = pivotTable.getFooterRows();
			final FooterRow footer;
			if (footerRows.isEmpty()) {
				footer = pivotTable.appendFooterRow();
			} else {
				footer = footerRows.get(0);
				footer.getCells().forEach(cell -> cell.setText("")); // remove old results
			}

			final String firstColumnId = pivotResult.getColumnIds().get(0);
			footer.getCell(pivotTable.getColumnByKey(firstColumnId)).setText("Grand Total");
			for (Map.Entry<String, Object> entry : pivotResult.grandTotalValues.entrySet()) {
				footer.getCell(pivotTable.getColumnByKey(entry.getKey())).setText("" + entry.getValue());
			}
		}
	}

}
