package org.vaadin.example.pivot.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.function.SerializableRunnable;
import org.jetbrains.annotations.NotNull;
import org.vaadin.jchristophe.SortableLayout;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Holds a list of components (items) of type {@link T}.
 * <p></p>
 * Allows you to add the
 * items programatically via {@link #addItem(Component)}, remove the item via {@link #removeItem(Component)},
 * but also allows the user to reorder the items in the Drag'n'Drop fashion.
 * <p></p>
 * Fires {@link #onItemListChanged} whenever the list of items change, either programatically
 * or by the user.
 * @author Martin Vysny <mavi@vaadin.com>
 */
public final class ItemsPane<T extends Component> extends Composite<Component> {
    @NotNull
    private final MenuBar addItemButton = new MenuBar();
    @NotNull
    private final MenuItem addItemMenu;
    @NotNull
    private final VerticalLayout content = new VerticalLayout();
    @NotNull
    private final VerticalLayout itemPane = new VerticalLayout();

    /**
     * Fired when the list of items changes, either via {@link #addItem(Component)},
     * {@link #removeItem(Component)} or {@link #clear()}, or by the user via the
     * drag'n'drop reordering.
     */
    @NotNull
    public SerializableRunnable onItemListChanged = () -> {};

    /**
     * Creates the items pane.
     * @param caption        the caption shown above all of the items.
     * @param addItemCaption the caption on the "Add Item" button. Use {@link #setAddOptions(LinkedHashMap)}
     *                       to populate the options in the button.
     */
    public ItemsPane(@NotNull String caption, @NotNull String addItemCaption) {
        content.setPadding(false);
        content.setSpacing(false);
        content.setMargin(false);
        itemPane.setPadding(false);
        itemPane.setSpacing(false);
        itemPane.setMargin(false);
        content.addClassName("pivot-group-by");
        final SortableLayout sortableItemPane = new SortableLayout(itemPane);
        sortableItemPane.setOnOrderChanged(e -> fireItemListChanged());
        content.add(new Span(caption), sortableItemPane, addItemButton);
        addItemMenu = addItemButton.addItem(addItemCaption);
        setAddOptions(new LinkedHashMap<>());
    }

    @Override
    protected Component initContent() {
        return content;
    }

    /**
     * Adds given item at the bottom of the list. Fires {@link #onItemListChanged}.
     * @param item the item to add, not null. The item must not be nested in any component.
     */
    public void addItem(@NotNull T item) {
        if (item.getParent().isPresent()) {
            throw new IllegalArgumentException("Parameter item: invalid value " + item + ": already present in a pane " + item.getParent().get());
        }
        itemPane.add(item);
        fireItemListChanged();
    }

    /**
     * Returns the current list of items.
     * @return the list of items, not null.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @NotNull
    public List<T> getItems() {
        return (List) itemPane.getChildren()
                .collect(Collectors.toList());
    }

    /**
     * Repopulates the list of options on the "Add Item" button.
     * @param options the list of options. Maps the String caption to a closure handling that action.
     *                May be empty - in such case the "Add Item" button will be removed.
     */
    public void setAddOptions(@NotNull LinkedHashMap<String, SerializableRunnable> options) {
        addItemMenu.getSubMenu().removeAll();
        for (Map.Entry<String, SerializableRunnable> entry : options.entrySet()) {
            addItemMenu.getSubMenu().addItem(entry.getKey(), e -> entry.getValue().run());
        }
        addItemButton.setVisible(!options.isEmpty());
    }

    /**
     * Removes given item from the container. Fires {@link #onItemListChanged}.
     * @param item the item to remove, not null. Must be present in this pane.
     */
    public void removeItem(@NotNull T item) {
        if (item.getParent().orElse(null) != itemPane) {
            throw new IllegalArgumentException("Parameter item: invalid value " + item + ": not nested in this pane");
        }
        itemPane.remove(item);
        fireItemListChanged();
    }

    /**
     * Removes all items from this pane. Fires {@link #onItemListChanged}.
     */
    public void clear() {
        if (itemPane.getComponentCount() > 0) {
            itemPane.removeAll();
            fireItemListChanged();
        }
    }

    private void fireItemListChanged() {
        onItemListChanged.run();
    }
}
