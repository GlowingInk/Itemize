package ink.glowing.itemize.util.rng;

import org.jetbrains.annotations.Nullable;

public interface WeightFunction<T> {
    double apply(@Nullable T t, int index);
}
