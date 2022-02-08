package org.vaadin.example;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.vaadin.example.exceptionassignment.component.ExceptionAssignmentSummaryGrid;
import org.vaadin.example.exceptionassignment.model.Associate;
import org.vaadin.example.exceptionassignment.model.AssociateBackend;
import org.vaadin.example.pivot.datasource.PivotDataSource;
import org.vaadin.example.pivot.datasource.PivotDataSource.Row;
import org.vaadin.example.pivot.ui.PivotConfigurationPanel;

/**
 * The main view contains a button and a click listener.
 */
@Route("exceptions")
public class AssetExceptionView extends VerticalLayout implements HasGrid<Row<AssetException>> {

    private ExceptionAssignmentSummaryGrid summaryGrid;
    private PivotConfigurationPanel<AssetException> pivotConfigurationPanel;
    private HorizontalLayout container;
    private Grid<Row<AssetException>> pivotTable;

    public AssetExceptionView() {
        setSizeFull();

        final Grid<AssetException> exceptionGrid = new Grid<>();
        exceptionGrid.addColumn(AssetException::getExceptionNumber).setHeader("Exception #").setSortable(true);
        exceptionGrid.addColumn(AssetException::getAssetClass).setHeader("Asset Class").setSortable(true);
        exceptionGrid.addColumn(AssetException::getSector).setHeader("Sector").setSortable(true);
        exceptionGrid.addColumn(AssetException::getRating).setHeader("Rating").setSortable(true);
        exceptionGrid.addColumn(AssetException::getCouponType).setHeader("Coupon Type").setSortable(true);
        exceptionGrid.addColumn(AssetException::getState).setHeader("State").setSortable(true);
        exceptionGrid.setItems(AssetException.ALL_EXCEPTIONS);
        add(exceptionGrid);

        add(new Button("Show Pivot Table", e -> {
            e.getSource().setVisible(false);
            exceptionGrid.setVisible(false);
            showPivotTable();
        }));
    }

    private void showPivotTable() {
        container = new HorizontalLayout();

        pivotConfigurationPanel = new PivotConfigurationPanel<AssetException>("Exceptions")
                .addGroupBy("Asset Class", AssetException::getAssetClass)
                .addGroupBy("Sector", AssetException::getSector)
                .addGroupBy("Rating", AssetException::getRating)
                .addGroupBy("Coupon Type", AssetException::getCouponType)
                .addGroupBy("State", AssetException::getState);

        summaryGrid = new ExceptionAssignmentSummaryGrid(AssociateBackend.getInstance(), this);
        pivotTable = createPivotTable(pivotConfigurationPanel);
        pivotConfigurationPanel.pivotConfigurationChangedListeners.add(() -> {
			List<GridSortOrder<Row<AssetException>>> sort = pivotTable.getSortOrder();
            final Grid<Row<AssetException>> newPivotTable = createPivotTable(pivotConfigurationPanel);
			List<GridSortOrder<Row<AssetException>>> newSort = copySort(sort, newPivotTable);
            container.replace(pivotTable, newPivotTable);
            pivotTable = newPivotTable;
            pivotTable.sort(newSort);
        });

        pivotConfigurationPanel.setFirstAsDefault();

        container.setWidthFull();
        container.addAndExpand(pivotTable);
        container.add(pivotConfigurationPanel);
        container.setSpacing(false);
        addAndExpand(container);
    }

    @NotNull
    private Grid<Row<AssetException>> createPivotTable(@NotNull PivotConfigurationPanel<AssetException> panel) {
        Grid<Row<AssetException>> pivotTable = PlayerPivotView.createPivotTable(panel, AssetException.ALL_EXCEPTIONS);

        pivotTable.setHeightFull();
//        pivotTable.addSelectionListener(event -> setSummaryGridVisibility(event.getFirstSelectedItem().isPresent()));
        pivotTable.addItemClickListener(event -> {
        	if (pivotTable.getSelectedItems().isEmpty()) {
            	setSummaryGridVisibility(false);        		
        	} else {
        		String key = event.getColumn().getKey();
        		Object value = event.getItem().get(key);
        		showDialog(event.getItem(),value);
        	}
        });


        return pivotTable;
    }

	private List<GridSortOrder<Row<AssetException>>> copySort(List<GridSortOrder<Row<AssetException>>> sort,
			final Grid<PivotDataSource.Row<AssetException>> newPivotTable) {
		List<GridSortOrder<Row<AssetException>>> newSort = new ArrayList<>();
		sort.forEach(s -> {
			String key = s.getSorted().getKey();
			// Since column instance is the key, we cannot simply use old sort order as is,
			// but we
			// need to create new copy. This works only if Grid columns have key assigned.
			GridSortOrder<Row<AssetException>> so = new GridSortOrder<Row<AssetException>>(newPivotTable.getColumnByKey(key),
					s.getDirection());
			newSort.add(so);
		});
		return newSort;
	}

	public void showDialog(Row<AssetException> item, Object value) {
        Dialog dialog = new Dialog();
        ComboBox<Associate> associateAssignmentField = new ComboBox<>();
        AssociateBackend backend = AssociateBackend.getInstance();
        associateAssignmentField.setItems(backend.streamAssociates().collect(Collectors.toList()));
        associateAssignmentField.setItemLabelGenerator(associate -> associate.getFullName());
        associateAssignmentField.addValueChangeListener(event -> { 
        	dialog.close();
            Notification.show("Associate " + event.getValue().getFullName() + " assigned to " + value);
        	setSummaryGridVisibility(true);
        });
        dialog.add(associateAssignmentField);
        dialog.open();
    }

    public void setSummaryGridVisibility(boolean show) {
        if (show) {
            container.remove(pivotConfigurationPanel);
            container.addAndExpand(summaryGrid);
        } else {
            container.remove(summaryGrid);
            container.add(pivotConfigurationPanel);
        }
    }

    @Override
    public Grid<Row<AssetException>> getGrid() {
        return pivotTable;
    }
}
