package org.vaadin.example.exceptionassignment.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Stefan Uebe
 */
public class AssociateBackend {

    private static final List<Associate> ASSOCIATES = new ArrayList<>();
    private static final AssociateBackend INSTANCE = new AssociateBackend();

    static {
        long idCounter = 1;
        for (int i = 1; i <= 10; i++) {
            Associate associate = new Associate(idCounter++,"Associate " + i);
            List<AssignmentRule> rules = new ArrayList<>();
            for (int j = 1; j <= 5; j++) {
                rules.add(new AssignmentRule(idCounter++, associate, "Exception type = " + j, (int) (Math.random() * 20.0d)));
            }
            associate.setRules(rules);
            ASSOCIATES.add(associate);
        }
    }

    public static AssociateBackend getInstance() {
        return INSTANCE;
    }

    public Stream<Associate> streamAssociates() {
        return ASSOCIATES
                .stream();
    }

    public Stream<Associate> streamAssociates(int offset, int limit) {
        return streamAssociates()
                .skip(offset)
                .limit(limit);
    }

    public Stream<AssignmentRule> streamRules(Associate associate, int offset, int limit) {
        return associate
                .getRulesToModify()
                .stream()
                .skip(offset)
                .limit(limit);
    }


    public int countAssociates() {
        return ASSOCIATES.size();
    }

    public int countRules(Associate associate) {
        return associate
                .getRulesToModify()
                .size();
    }

    public int sumAssignedExceptions() {
        return ASSOCIATES.stream()
                .mapToInt(Associate::sumAllCountByRule)
                .sum();
    }

    public int sumUnassignedExceptions() {
        return 0;
    }

    public int sumTotalExceptions() {
        return sumAssignedExceptions() + sumUnassignedExceptions();
    }

    /**
     * Deletes the given rules from the backend. In this demo, it will also affect already loaded assignment rules.
     * Does not remove the parent from the respective rules.
     *
     * @param rules rules to delete
     */
    public void delete(Collection<AssignmentRule> rules) {
        for (AssignmentRule rule : rules) {
            rule.getParent().getRulesToModify().remove(rule);
        }
    }

}
