package org.vaadin.example.exceptionassignment.component;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.FooterRow;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.function.SerializableSupplier;
import org.vaadin.example.HasGrid;
import org.vaadin.example.exceptionassignment.model.AssociateBackend;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Stefan Uebe
 */
public class ExceptionAssignmentSummaryGrid extends VerticalLayout {

    private final AssociateBackend backend;
    private final TreeGrid<GridItem> grid;

    private final Set<GridItem> selectedItems = new HashSet<>();
    private final Button deleteButton;

    public ExceptionAssignmentSummaryGrid(AssociateBackend backend, HasGrid<?> masterGridProvider) {
        this.backend = backend;
        setSizeFull();

        deleteButton = new Button("Delete", this::onDelete);
        deleteButton.setEnabled(false);

        Button closeButton = new Button(VaadinIcon.CLOSE.create(), event -> masterGridProvider.getGrid().deselectAll());

        Span placeholder = new Span();
        HorizontalLayout toolbar = new HorizontalLayout(deleteButton, placeholder, closeButton);
        toolbar.setWidthFull();
        toolbar.setFlexGrow(1, placeholder);
        add(toolbar);

        grid = new TreeGrid<>();
        grid.addHierarchyColumn(GridItem::getAssociate)
                .setHeader("Associate")
                .setKey("associate")
                .setWidth("150px")
                .setFlexGrow(1)
                .setSortable(false)
                .setResizable(true); // sorting disabled for now, activate later?

        grid.addColumn(GridItem::getCountByAssociate)
                .setHeader("Count by Associate")
                .setKey("countAssociate")
//                .setAutoWidth(true)
                .setSortable(false)
                .setResizable(true);

        grid.addComponentColumn(this::initRuleSelectionColumn)
                .setKey("rulesCheckbox")
                .setAutoWidth(true)
                .setSortable(false)
                .setResizable(false);

        grid.addColumn(GridItem::getAssignmentRule)
                .setHeader("Assignment Rules")
                .setKey("rules")
//                .setAutoWidth(true)
                .setSortable(false)
                .setResizable(true);

        grid.addColumn(GridItem::getCountByRule)
                .setHeader("Count by Rule")
                .setKey("countRules")
//                .setAutoWidth(true)
                .setSortable(false)
                .setResizable(true);

        grid.appendFooterRow();
        grid.appendFooterRow();
        grid.appendFooterRow();

        refreshFooterRows();

        GridItemHierarchicalDataProvider provider = new GridItemHierarchicalDataProvider(backend);
        grid.setDataProvider(provider);

        // if the grid will have a big bunch of items, this should be removed, otherwise it can affect performance and
        // memory usage a lot
        grid.expand(provider.fetchAssociatesToExpand());

        grid.setSelectionMode(Grid.SelectionMode.NONE);

        grid.addItemClickListener(event -> {
            GridItem item = event.getItem();
            if (item.isRule()) {
                onRuleSelected(item, !selectedItems.contains(item));
                grid.getDataProvider().refreshItem(item);
            }
        });

        addAndExpand(grid);
    }

    private Component initRuleSelectionColumn(GridItem item) {
        if (item.isAssociate()) {
            return new Span();
        }
        Checkbox checkbox = new Checkbox();
        checkbox.setValue(selectedItems.contains(item));
        checkbox.addValueChangeListener(event -> onRuleSelected(item, event));
        return checkbox;
    }

    private void refreshFooterRows() {
        List<FooterRow> footerRows = grid.getFooterRows();
        refreshFooterRow(footerRows.get(0), "Assigned Exceptions", backend::sumAssignedExceptions);
        refreshFooterRow(footerRows.get(1), "Unassigned Exceptions", backend::sumUnassignedExceptions);
        refreshFooterRow(footerRows.get(2), "Total Exceptions", backend::sumTotalExceptions);
    }

    private void refreshFooterRow(FooterRow footerRow, String text, SerializableSupplier<Integer> valueCallback) {
        footerRow.getCell(grid.getColumnByKey("associate")).setText(text);
        footerRow.getCell(grid.getColumnByKey("countAssociate")).setText(String.valueOf(valueCallback.get()));
    }

    private void onRuleSelected(GridItem item, ComponentValueChangeEvent<Checkbox, Boolean> event) {
        onRuleSelected(item, event.getValue());
    }

    private void onRuleSelected(GridItem item, boolean selected) {
        if (selected) {
            selectedItems.add(item);
        } else {
            selectedItems.remove(item);
        }

        deleteButton.setEnabled(!selectedItems.isEmpty());
    }

    private void onDelete(ClickEvent<Button> event) {
        backend.delete(selectedItems
                .stream()
                .map(GridItem::asRule)
                .map(RuleItem::getWrapped)
                .collect(Collectors.toSet()));
        selectedItems.clear();
        grid.getDataProvider().refreshAll();
        refreshFooterRows();

    }

}
