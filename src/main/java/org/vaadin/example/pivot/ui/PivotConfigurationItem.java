package org.vaadin.example.pivot.ui;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents one item in the {@link PivotConfigurationPanel} - one group-by clause
 * or one aggregate clause.
 * @author Martin Vysny <mavi@vaadin.com>
 */
public class PivotConfigurationItem extends HorizontalLayout {
    private final Span caption = new Span();
    private final PivotIconButton closeButton = new PivotIconButton(VaadinIcon.CLOSE_CIRCLE);
    @Nullable
    private Component currentSlotComponent = null;
    public PivotConfigurationItem(@NotNull String caption) {
        setWidthFull();
        setHeight("31px");
        addClassName("pivot-config-item");
        final PivotIconButton dragHandle = new PivotIconButton(VaadinIcon.MENU);
        dragHandle.addClassName("drag-handle");
        add(dragHandle);
        addAndExpand(this.caption);
        setAlignItems(Alignment.CENTER);
        add(closeButton);
        setCaption(caption);
    }

    public void setCaption(@NotNull String caption) {
        this.caption.setText(caption);
    }

    public void addCloseListener(@NotNull ComponentEventListener<ClickEvent<Icon>> e) {
        closeButton.addClickListener(e);
    }

    public void setSlotComponent(@Nullable Component component) {
        if (currentSlotComponent != null) {
            remove(currentSlotComponent);
        }
        if (component != null) {
            getElement().insertChild(getComponentCount() - 1, component.getElement());
            currentSlotComponent = component;
        }
    }

    @Nullable
    public Component getSlotComponent() {
        return currentSlotComponent;
    }
}
