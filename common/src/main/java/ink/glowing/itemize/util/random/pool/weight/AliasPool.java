package ink.glowing.itemize.util.random.pool.weight;

import ink.glowing.itemize.util.random.pool.RandomPool;
import ink.glowing.itemize.util.random.pool.SingletonPool;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.IntToDoubleFunction;
import java.util.function.ToDoubleFunction;
import java.util.random.RandomGenerator;

import static ink.glowing.itemize.util.random.pool.SingletonPool.emptyPool;
import static ink.glowing.itemize.util.random.pool.UniformPool.uniformPool;

/**
 * Based off Keith Schwarz's (htiek@cs.stanford.edu) AliasMethod.java
 * <a href="http://www.keithschwarz.com/darts-dice-coins/">darts-dice-coins</a>
 * @param <T> Type of elements
 */
public class AliasPool<T> implements RandomPool<T> {
    private final List<T> elements;

    private final int[] alias;
    private final double[] probabilities;

    private AliasPool(@NotNull List<T> elements, double[] weights, double weightsSum) {
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
    public T next(@NotNull RandomGenerator rng) {
        double number = rng.nextDouble();
        int column = (int) (((number * Integer.MAX_VALUE) % 1.0) * this.probabilities.length);
        boolean coinToss = number < this.probabilities[column];
        return this.elements.get(coinToss ? column : this.alias[column]);
    }

    static <T> @NotNull RandomPool<T> aliasPool(@NotNull Map<T, Double> elements) {
        return aliasPool(elements.keySet(), (t, _) -> {
            Double weight = elements.getOrDefault(t, 0.0);
            return weight != null ? weight : 0.0;
        });
    }

    static <T> @NotNull RandomPool<T> aliasPoolIndexed(@NotNull Iterable<T> iterable, @NotNull IntToDoubleFunction funct) {
        return aliasPool(iterable, (_, i) -> funct.applyAsDouble(i));
    }

    static <T> @NotNull RandomPool<T> aliasPool(@NotNull Iterable<T> iterable, @NotNull ToDoubleFunction<T> funct) {
        return aliasPool(iterable, (t, _) -> funct.applyAsDouble(t));
    }

    static <T> @NotNull RandomPool<T> aliasPool(@NotNull Iterable<T> iterable, @NotNull WeightFunction<T> funct) {
        return switch (iterable) {
            case SequencedCollection<T> collection -> aliasPool(collection, funct);
            case Collection<T> collection -> aliasPool(collection, funct);
            default -> {
                List<T> asList = new ArrayList<>();
                for (T item : iterable) asList.add(item);
                yield aliasPool(asList, funct);
            }
        };
    }

    static <T> @NotNull RandomPool<T> aliasPoolIndexed(@NotNull Collection<T> collection, @NotNull IntToDoubleFunction funct) {
        return aliasPool(collection, (_, i) -> funct.applyAsDouble(i));
    }

    static <T> @NotNull RandomPool<T> aliasPool(@NotNull Collection<T> collection, @NotNull ToDoubleFunction<T> funct) {
        return aliasPool(collection, (t, _) -> funct.applyAsDouble(t));
    }

    static <T> @NotNull RandomPool<T> aliasPool(@NotNull Collection<T> collection, @NotNull WeightFunction<T> funct) {
        return switch (collection.size()) {
            case 0 -> emptyPool();
            case 1 -> new SingletonPool<>(collection.iterator().next());
            default -> _aliasPool(collection, funct);
        };
    }

    static <T> @NotNull RandomPool<T> aliasPoolIndexed(@NotNull SequencedCollection<T> collection, @NotNull IntToDoubleFunction funct) {
        return aliasPool(collection, (_, i) -> funct.applyAsDouble(i));
    }

    static <T> @NotNull RandomPool<T> aliasPool(@NotNull SequencedCollection<T> collection, @NotNull ToDoubleFunction<T> funct) {
        return aliasPool(collection, (t, _) -> funct.applyAsDouble(t));
    }

    static <T> @NotNull RandomPool<T> aliasPool(@NotNull SequencedCollection<T> collection, @NotNull WeightFunction<T> funct) {
        return switch (collection.size()) {
            case 0 -> emptyPool();
            case 1 -> new SingletonPool<>(collection.getFirst());
            default -> _aliasPool(collection, funct);
        };
    }

    private static <T> @NotNull RandomPool<T> _aliasPool(@NotNull Collection<T> collection, @NotNull WeightFunction<T> funct) {
        double[] rawWeights = new double[collection.size()];
        ArrayList<T> filteredElements = new ArrayList<>(collection.size());
        List<T> negativeElements = new ArrayList<>(0);
        double negativeWeight = Double.NEGATIVE_INFINITY;
        double weightsSum = 0;
        int index = 0;
        for (T item : collection) {
            double weight = funct.apply(item, index++);
            if (weight <= 0) {
                if (filteredElements.isEmpty()) {
                    if (weight > negativeWeight) {
                        negativeWeight = weight;
                        negativeElements.clear();
                        negativeElements.add(item);
                    } else if (weight == negativeWeight) {
                        negativeElements.add(item);
                    }
                }
                continue;
            }
            filteredElements.add(item);
            rawWeights[filteredElements.size() - 1] = weight;
            weightsSum += weight;
        }

        return switch (filteredElements.size()) {
            case 0 -> uniformPool(negativeElements);
            case 1 -> new SingletonPool<>(filteredElements.getFirst());
            default -> {
                filteredElements.trimToSize();
                yield new AliasPool<>(filteredElements, rawWeights, weightsSum);
            }
        };
    }
}
