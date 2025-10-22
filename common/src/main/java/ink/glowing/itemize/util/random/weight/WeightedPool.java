package ink.glowing.itemize.util.random.weight;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.ToDoubleFunction;
import java.util.random.RandomGenerator;
import java.util.stream.Stream;

@FunctionalInterface
public interface WeightedPool<T> {
    static <T> @NotNull WeightedPool<T> emptyPool() {
        //noinspection unchecked
        return (WeightedPool<T>) SingletonPool.EMPTY;
    }

    static <T> @NotNull WeightedPool<T> weightedPool(@Nullable T t) {
        return new SingletonPool<>(t);
    }

    static <T> @NotNull WeightedPool<T> weightedPool(@NotNull Map<T, Double> elements) {
        return weightedPool(elements.keySet(), (t, _) -> {
            Double weight = elements.getOrDefault(t, 0.0);
            return weight != null ? weight : 0.0;
        });
    }

    static <T> @NotNull WeightedPool<T> weightedPool(@NotNull Iterable<T> iterable, @NotNull ToDoubleFunction<T> funct) {
        return weightedPool(iterable, (t, _) -> funct.applyAsDouble(t));
    }

    static <T> @NotNull WeightedPool<T> weightedPool(@NotNull Iterable<T> iterable, @NotNull WeightFunction<T> funct) {
        return switch (iterable) {
            case SequencedCollection<T> sequenced -> weightedPool(sequenced, funct);
            case Collection<T> collection -> weightedPool(collection, funct);
            default -> {
                List<T> asList = new ArrayList<>();
                for (T item : iterable) asList.add(item);
                yield weightedPool(asList, funct);
            }
        };
    }

    static <T> @NotNull WeightedPool<T> weightedPool(@NotNull Collection<T> collection, @NotNull ToDoubleFunction<T> funct) {
        return weightedPool(collection, (t, _) -> funct.applyAsDouble(t));
    }

    static <T> @NotNull WeightedPool<T> weightedPool(@NotNull Collection<T> collection, @NotNull WeightFunction<T> funct) {
        return switch (collection.size()) {
            case 0 -> emptyPool();
            case 1 -> weightedPool(collection.iterator().next());
            default -> AliasMethod.tryAlias(collection, funct);
        };
    }

    static <T> @NotNull WeightedPool<T> weightedPool(@NotNull SequencedCollection<T> collection, @NotNull ToDoubleFunction<T> funct) {
        return weightedPool(collection, (t, _) -> funct.applyAsDouble(t));
    }

    static <T> @NotNull WeightedPool<T> weightedPool(@NotNull SequencedCollection<T> collection, @NotNull WeightFunction<T> funct) {
        return switch (collection.size()) {
            case 0 -> emptyPool();
            case 1 -> weightedPool(collection.getFirst());
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
            double[] rawWeights = new double[collection.size()];
            ArrayList<T> filteredElements = new ArrayList<>(collection.size());
            double weightsSum = 0;
            int index = 0;
            for (T item : collection) {
                double weight = funct.apply(item, index++);
                if (weight <= 0) continue;
                filteredElements.add(item);
                rawWeights[filteredElements.size() - 1] = weight;
                weightsSum += weight;
            }

            return switch (filteredElements.size()) {
                case 0 -> emptyPool();
                case 1 -> weightedPool(filteredElements.getFirst());
                default -> {
                    filteredElements.trimToSize();
                    yield new AliasMethod<>(filteredElements, rawWeights, weightsSum);
                }
            };
        }

        private AliasMethod(@NotNull List<T> elements, double[] weights, double weightsSum) {
            int size = elements.size();

            this.elements = elements;
            this.probabilities = new double[size];
            this.alias = new int[size];

            double averageProbability = 1.0 / size;

            Deque<Integer> small = new ArrayDeque<>();
            Deque<Integer> large = new ArrayDeque<>();

            for (int i = 0; i < size; ++i) {
                if ((weights[i] /= weightsSum) < averageProbability) {
                    small.add(i);
                } else {
                    large.add(i);
                }
            }

            while (!small.isEmpty() && !large.isEmpty()) {
                int less = small.removeLast();
                int more = large.removeLast();

                this.probabilities[less] = weights[less] * size;
                this.alias[less] = more;

                weights[more] += weights[less] - averageProbability;
                if (weights[more] < averageProbability) {
                    small.add(more);
                } else {
                    large.add(more);
                }
            }

            while (!small.isEmpty()) this.probabilities[small.removeLast()] = 1.0;
            while (!large.isEmpty()) this.probabilities[large.removeLast()] = 1.0;
        }

        @Override
        public @Nullable T next(@NotNull RandomGenerator rng) {
            double number = rng.nextDouble();
            int column = (int) (((number * Integer.MAX_VALUE) % 1.0) * this.probabilities.length);
            boolean coinToss = number < this.probabilities[column];
            return this.elements.get(coinToss ? column : this.alias[column]);
        }
    }

    record SingletonPool<T>(@Nullable T value) implements WeightedPool<T> {
        static final SingletonPool<?> EMPTY = new SingletonPool<>(null);

        @Override
        public @Nullable T next(@NotNull RandomGenerator rng) {
            return value;
        }

        @Override
        public @NotNull Stream<@Nullable T> stream(@NotNull RandomGenerator rng) {
            return Stream.generate(() -> value);
        }
    }
}
