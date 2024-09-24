package ink.glowing.itemize.util.rng;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.ToDoubleFunction;
import java.util.random.RandomGenerator;

public interface WeightedPool<T> {
    static <T> @NotNull WeightedPool<T> weightedPool(@NotNull Collection<T> collection, @NotNull ToDoubleFunction<T> funct) {
        return weightedPool(collection, WeightFunction.fromIndexUnaware(funct));
    }

    static <T> @NotNull WeightedPool<T> weightedPool(@NotNull Collection<T> collection, @NotNull WeightFunction<T> funct) {
        return switch (collection.size()) {
            case 0 -> (rng) -> null;
            case 1 -> {
                T elem = collection.iterator().next();
                yield (rng) -> elem;
            }
            default -> new AliasMethod<>(collection, funct);
        };
    }

    @Nullable T next(@NotNull RandomGenerator rng);

    /**
     * Based off Keith Schwarz's (htiek@cs.stanford.edu) AliasMethod.java
     * <a href="http://www.keithschwarz.com/darts-dice-coins/">darts-dice-coins</a>
     * @param <T> Type of elements
     */
    class AliasMethod<T> implements WeightedPool<T> {
        private final List<T> elements;

        private final int[] alias;
        private final double[] probabilities;

        private AliasMethod(@NotNull Collection<T> collection, @NotNull WeightFunction<T> funct) {
            final int size = collection.size();

            double[] rawProbabilities = new double[size];
            this.elements = new ArrayList<>(size);

            double sum = 0;
            {
                int index = 0;
                for (T item : collection) {
                    elements.add(item);
                    sum += (rawProbabilities[index] = funct.apply(item, index++));
                }
            }

            this.probabilities = new double[size];
            this.alias = new int[size];

            final double average = 1d / size;

            Deque<Integer> small = new ArrayDeque<>();
            Deque<Integer> large = new ArrayDeque<>();

            for (int i = 0; i < size; ++i) {
                if ((rawProbabilities[i] /= sum) >= average) {
                    large.add(i);
                } else {
                    small.add(i);
                }
            }

            while (!small.isEmpty() && !large.isEmpty()) {
                int less = small.removeLast();
                int more = large.removeLast();

                this.probabilities[less] = rawProbabilities[less] * size;
                this.alias[less] = more;

                rawProbabilities[more] += rawProbabilities[less] - average;
                (rawProbabilities[more] >= average ? large : small).add(more);
            }

            while (!small.isEmpty()) this.probabilities[small.removeLast()] = 1;
            while (!large.isEmpty()) this.probabilities[large.removeLast()] = 1;
        }

        @Override
        public @Nullable T next(@NotNull RandomGenerator rng) {
            int column = rng.nextInt(probabilities.length);
            boolean coinToss = rng.nextDouble() < probabilities[column];
            return elements.get(coinToss ? column : alias[column]);
        }
    }
}
