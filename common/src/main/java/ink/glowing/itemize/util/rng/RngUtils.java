package ink.glowing.itemize.util.rng;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

public final class RngUtils {
    private RngUtils() { }

    public static @NotNull RandomGenerator localRandom() {
        return ThreadLocalRandom.current();
    }

    public static <T> @Nullable T next(@NotNull RandomGenerator rng, @NotNull List<? extends T> list) {
        return list.get(rng.nextInt(list.size()));
    }

    public static <T> @Nullable T next(@NotNull RandomGenerator rng, T @NotNull [] array) {
        return array[rng.nextInt(array.length)];
    }
}
