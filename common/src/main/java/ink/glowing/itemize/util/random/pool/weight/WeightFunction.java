package ink.glowing.itemize.util.random.pool.weight;

import org.jetbrains.annotations.Nullable;

public interface WeightFunction<T> {
    double apply(@Nullable T t, int index);
}
