/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.properties;

import java.util.Objects;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Represents a range of comparable values with an optional minimum and an optional maximum.
 *
 * @param <T> The type of the values in the range, must be Comparable.
 *
 */
public final class Range<T extends Comparable<T>> {

    private final @Nullable T min;
    private final @Nullable T max;

    public Range(@Nullable T min, @Nullable T max) {
        // only check if both are non-null. If one is null, it's an unbounded range
        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new IllegalArgumentException("min must be less than or equal to max when both are specified");
        }
        this.min = min;
        this.max = max;
    }

    public @Nullable T getMin() {
        return min;
    }

    public @Nullable T getMax() {
        return max;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Range<?> range = (Range<?>) o;
        return Objects.equals(min, range.min)
            && Objects.equals(max, range.max);
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max);
    }

    @Override
    public String toString() {
        return "Range{"
            + "min=" + (min != null ? min : "unbounded")
            + ", max=" + (max != null ? max : "unbounded")
            + '}';
    }
}
