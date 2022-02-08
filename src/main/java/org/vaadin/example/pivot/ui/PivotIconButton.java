package org.vaadin.example.pivot.ui;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import org.jetbrains.annotations.NotNull;

/**
 * A small 16x16px button, used throughout the {@link PivotConfigurationPanel}.
 * @author Martin Vysny <mavi@vaadin.com>
 */
public class PivotIconButton extends Icon {
    public PivotIconButton(@NotNull VaadinIcon icon) {
        super(icon);
        addClassName("pivot-icon-button");
        setSize("16px");
    }
}
