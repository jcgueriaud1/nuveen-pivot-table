package org.vaadin.example;

import com.vaadin.flow.component.grid.Grid;

/**
 * @author Stefan Uebe
 */
public interface HasGrid<T> {

    Grid<T> getGrid();

}
