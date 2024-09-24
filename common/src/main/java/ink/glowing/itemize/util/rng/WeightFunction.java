package ink.glowing.itemize.util.rng;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.ToDoubleFunction;

public interface WeightFunction<T> {
    double apply(@Nullable T t, int index);

    static <T> @NotNull WeightFunction<T> fromIndexUnaware(@NotNull ToDoubleFunction<T> funct) {
        return (t, index) -> funct.applyAsDouble(t);
    }
}
