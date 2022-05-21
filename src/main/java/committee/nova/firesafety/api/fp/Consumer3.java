package committee.nova.firesafety.api.fp;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

@FunctionalInterface
public interface Consumer3<T1, T2, T3> {
    void accept(T1 t1, T2 t2, T3 t3);

    default Function<T1, BiConsumer<T2, T3>> curry() {
        return t1 -> (t2, t3) -> accept(t1, t2, t3);
    }

    default BiFunction<T1, T2, Consumer<T3>> curry2() {
        return (t1, t2) -> t3 -> accept(t1, t2, t3);
    }

    default Consumer3<T1, T2, T3> andThen(Consumer3<? super T1, ? super T2, ? super T3> after) {
        Objects.requireNonNull(after);
        return (a, b, c) -> {
            accept(a, b, c);
            after.accept(a, b, c);
        };
    }
}
