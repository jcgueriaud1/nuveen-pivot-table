package org.vaadin.example.exceptionassignment.component;

import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import org.vaadin.example.exceptionassignment.model.AssociateBackend;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Stefan Uebe
 */
public class GridItemHierarchicalDataProvider extends AbstractBackEndHierarchicalDataProvider<GridItem, Void> {

    private final AssociateBackend backend;

    public GridItemHierarchicalDataProvider(AssociateBackend backend) {
        this.backend = backend;
    }

    public Collection<GridItem> fetchAssociatesToExpand() {
        return fetchChildrenFromBackEnd(new HierarchicalQuery<>(null, null)).collect(Collectors.toSet());
    }

    @Override
    protected Stream<GridItem> fetchChildrenFromBackEnd(HierarchicalQuery<GridItem, Void> query) {
        GridItem parent = query.getParent();

        // no sorting supported for now
        if (parent == null) {
            return backend
                    .streamAssociates(query.getOffset(), query.getLimit())
                    .map(AssociateItem::new);
        }

        return backend
                .streamRules(parent.asAssociate().getWrapped(), query.getOffset(), query.getLimit())
                .map(RuleItem::new);
    }

    @Override
    public int getChildCount(HierarchicalQuery<GridItem, Void> query) {
        GridItem parent = query.getParent();

        if (parent == null) {
            return backend.countAssociates();
        }

        return backend.countRules(parent.asAssociate().getWrapped());
    }

    @Override
    public boolean hasChildren(GridItem item) {
        return item != null && item.isAssociate() && backend.countRules(item.asAssociate().getWrapped()) > 0;
    }
}
