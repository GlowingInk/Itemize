package ink.glowing.itemize.util;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.random.RandomGenerator;

public interface WeightedPool<T> extends Function<RandomGenerator, T> {
    static <T> @NotNull WeightedPool<T> weightedPool(@NotNull Collection<T> collection, @NotNull ToDoubleFunction<T> funct) {
        return switch (collection.size()) {
            case 0 -> (rng) -> null;
            case 1 -> {
                T elem = collection.stream().findAny().get();
                yield (rng) -> elem;
            }
            default -> new AliasMethod<>(collection, funct);
        };
    }

    /**
     * Based off Keith Schwarz's (htiek@cs.stanford.edu) AliasMethod.java
     * <a href="http://www.keithschwarz.com/darts-dice-coins/">darts-dice-coins</a>
     * @param <T> Type of elements
     */
    class AliasMethod<T> implements WeightedPool<T> {
        private final List<T> elements;

        private final int[] alias;
        private final double[] probability;

        private AliasMethod(@NotNull Collection<T> collection, @NotNull ToDoubleFunction<T> funct) {
            final int size = collection.size();

            double[] probabilities = new double[size];
            this.elements = new ArrayList<>(size);

            double sum = 0;
            int j = 0;
            for (T item : collection) {
                elements.add(item);
                sum += (probabilities[j++] = funct.applyAsDouble(item));
            }

            this.probability = new double[size];
            this.alias = new int[size];

            final double average = 1d / size;

            Deque<Integer> small = new ArrayDeque<>();
            Deque<Integer> large = new ArrayDeque<>();

            for (int i = 0; i < size; ++i) {
                if ((probabilities[i] /= sum) >= average) {
                    large.add(i);
                } else {
                    small.add(i);
                }
            }

            while (!small.isEmpty() && !large.isEmpty()) {
                int less = small.removeLast();
                int more = large.removeLast();

                this.probability[less] = probabilities[less] * size;
                this.alias[less] = more;

                probabilities[more] += probabilities[less] - average;

                if (probabilities[more] >= average) {
                    large.add(more);
                } else {
                    small.add(more);
                }
            }

            while (!small.isEmpty()) {
                this.probability[small.removeLast()] = 1;
            }
            while (!large.isEmpty()) {
                this.probability[large.removeLast()] = 1;
            }
        }

        @Override
        public T apply(@NotNull RandomGenerator rng) {
            double value = rng.nextDouble();
            int column = (int) (probability.length * value);
            return elements.get(
                    value > probability[column]
                            ? column
                            : alias[column]
            );
        }
    }
}
