package ink.glowing.itemize.util.rng;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;
import java.util.random.RandomGenerator;
import java.util.stream.Stream;

public interface WeightedPool<T> {
    static <T> @NotNull WeightedPool<T> emptyPool() {
        return (rng) -> null;
    }

    static <T> @NotNull WeightedPool<T> weightedPool(@NotNull T t) {
        return (rng) -> t;
    }

    static <T> @NotNull WeightedPool<T> weightedPool(@NotNull Map<T, Double> elements) {
        return weightedPool(elements.keySet(), (t, i) -> elements.getOrDefault(t, 0d));
    }

    static <T> @NotNull WeightedPool<T> weightedPool(@NotNull Collection<T> collection, @NotNull ToDoubleFunction<T> funct) {
        return weightedPool(collection, (t, i) -> funct.applyAsDouble(t));
    }

    static <T> @NotNull WeightedPool<T> weightedPool(@NotNull Collection<T> collection, @NotNull WeightFunction<T> funct) {
        return switch (collection.size()) {
            case 0 -> emptyPool();
            case 1 -> {
                T elem = collection.iterator().next();
                yield funct.apply(elem, 0) > 0
                        ? weightedPool(elem)
                        : emptyPool();
            }
            default -> AliasMethod.tryAlias(collection, funct);
        };
    }

    @Nullable T next(@NotNull RandomGenerator rng);

    default @NotNull Stream<@Nullable T> stream(@NotNull RandomGenerator rng) {
        return Stream.generate(() -> next(rng));
    }

    /**
     * Based off Keith Schwarz's (htiek@cs.stanford.edu) AliasMethod.java
     * <a href="http://www.keithschwarz.com/darts-dice-coins/">darts-dice-coins</a>
     * @param <T> Type of elements
     */
    class AliasMethod<T> implements WeightedPool<T> {
        private final List<T> elements;

        private final int[] alias;
        private final double[] probabilities;

        private static <T> @NotNull WeightedPool<T> tryAlias(@NotNull Collection<T> collection, @NotNull WeightFunction<T> funct) {
            double[] rawProbabilities = new double[collection.size()];
            ArrayList<T> elements = new ArrayList<>(collection.size());

            double weightsSum = 0;
            int index = 0;
            for (T item : collection) {
                double weight = funct.apply(item, index++);
                if (weight <= 0) continue;
                elements.add(item);
                rawProbabilities[elements.size() - 1] = weight;
                weightsSum += weight;
            }

            return switch (elements.size()) {
                case 0 -> emptyPool();
                case 1 -> weightedPool(elements.getFirst());
                default -> {
                    elements.trimToSize();
                    yield new AliasMethod<>(elements, rawProbabilities, weightsSum);
                }
            };
        }

        private AliasMethod(@NotNull List<T> elements, double[] rawProbabilities, double weightsSum) {
            int size = elements.size();

            this.elements = elements;
            this.probabilities = new double[size];
            this.alias = new int[size];

            double averageProbability = 1d / size;

            int[] small = new int[size]; int smallSize = 0;
            int[] large = new int[size]; int largeSize = 0;

            for (int i = 0; i < size; ++i) {
                if ((rawProbabilities[i] /= weightsSum) < averageProbability) {
                    small[smallSize++] = i;
                } else {
                    large[largeSize++] = i;
                }
            }

            while (smallSize != 0 && largeSize != 0) {
                int less = small[--smallSize];
                int more = large[--largeSize];

                this.probabilities[less] = rawProbabilities[less] * size;
                this.alias[less] = more;

                rawProbabilities[more] += rawProbabilities[less] - averageProbability;
                if (rawProbabilities[more] < averageProbability) {
                    small[smallSize++] = more;
                } else {
                    large[largeSize++] = more;
                }
            }

            while (smallSize != 0) this.probabilities[small[--smallSize]] = 1;
            while (largeSize != 0) this.probabilities[large[--largeSize]] = 1;
        }

        @Override
        public @Nullable T next(@NotNull RandomGenerator rng) {
            int column = rng.nextInt(this.probabilities.length);
            boolean coinToss = rng.nextDouble() < this.probabilities[column];
            return this.elements.get(coinToss ? column : this.alias[column]);
        }
    }
}
