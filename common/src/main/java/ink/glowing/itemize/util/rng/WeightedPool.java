package ink.glowing.itemize.util.rng;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.function.ToDoubleFunction;
import java.util.random.RandomGenerator;
import java.util.stream.Stream;

public interface WeightedPool<T> {
    static <T> @NotNull WeightedPool<T> weightedPool(@NotNull Map<T, Double> elements) {
        return weightedPool(elements.keySet(), (t, i) -> elements.getOrDefault(t, 0d));
    }

    static <T> @NotNull WeightedPool<T> weightedPool(@NotNull Collection<T> collection, @NotNull ToDoubleFunction<T> funct) {
        return weightedPool(collection, (t, i) -> funct.applyAsDouble(t));
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

    default @NotNull Stream<@Nullable T> stream(@NotNull RandomGenerator rng) {
        return Stream.generate(() -> next(rng));
    }

    /**
     * Based off Keith Schwarz's (htiek@cs.stanford.edu) AliasMethod.java
     * <a href="http://www.keithschwarz.com/darts-dice-coins/">darts-dice-coins</a>
     * @param <T> Type of elements
     */
    class AliasMethod<T> implements WeightedPool<T> {
        private final ArrayList<T> elements;

        private final int[] alias;
        private final double[] probabilities;

        private AliasMethod(@NotNull Collection<T> collection, @NotNull WeightFunction<T> funct) {
            double[] rawProbabilities = new double[collection.size()];
            elements = new ArrayList<>(collection.size());

            double weightsSum = 0;
            {
                int index = 0;
                for (T item : collection) {
                    double weight = funct.apply(item, index++);
                    if (weight <= 0) continue;
                    elements.add(item);
                    rawProbabilities[elements.size()] = weight;
                    weightsSum += weight;
                }
                elements.trimToSize();
            }

            int size = elements.size();

            probabilities = new double[size];
            alias = new int[size];

            double averageProbability = 1d / size;

            int[] small = new int[size]; int smallSize = 0;
            int[] large = new int[size]; int largeSize = 0;

            for (int i = 0; i < size; ++i) {
                if (!((rawProbabilities[i] /= weightsSum) >= averageProbability)) {
                    small[smallSize++] = i;
                } else {
                    large[largeSize++] = i;
                }
            }

            while (largeSize != 0 && smallSize != 0) {
                int less = small[--smallSize];
                int more = large[--largeSize];

                probabilities[less] = rawProbabilities[less] * size;
                alias[less] = more;

                rawProbabilities[more] += rawProbabilities[less] - averageProbability;
                if (rawProbabilities[more] < averageProbability) {
                    small[smallSize++] = more;
                } else {
                    large[largeSize++] = more;
                }
            }

            while (smallSize != 0) probabilities[small[--smallSize]] = 1;
            while (largeSize != 0) probabilities[large[--largeSize]] = 1;
        }

        @Override
        public @Nullable T next(@NotNull RandomGenerator rng) {
            int column = rng.nextInt(probabilities.length);
            boolean coinToss = rng.nextDouble() < probabilities[column];
            return elements.get(coinToss ? column : alias[column]);
        }
    }
}
