package org.vaadin.example.pivot.datasource;

import com.vaadin.flow.function.SerializableFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Objects;

/**
 * A definition for one pivotable property: contains a caption (the name of the property) and {@link #valueProvider}
 * which extracts the numeric value. Doesn't compute the aggregated
 * value itself - for that see {@link Aggregate}.
 */
public final class PivotProperty<T> implements Serializable {
    @NotNull
    public final String caption;
    /**
     * Extracts a value out of the bean. Usually produces a Number, however
     * in specific cases of providing a count of beans this can simply be an identity function.
     */
    @NotNull
    public final SerializableFunction<T, ?> valueProvider;

    public PivotProperty(@NotNull String caption, @NotNull SerializableFunction<T, ?> valueProvider) {
        this.caption = Objects.requireNonNull(caption);
        this.valueProvider = Objects.requireNonNull(valueProvider);
    }

    @Override
    public String toString() {
        return "PivotProperty{" +
                "caption='" + caption + '\'' +
                ", valueProvider=" + valueProvider +
                '}';
    }

    /**
     * Returns the value of the property for given bean.
     * @param bean the bean
     * @return the value of this property, may be null.
     */
    @Nullable
    public Object getValue(@NotNull T bean) {
        return valueProvider.apply(bean);
    }
}
