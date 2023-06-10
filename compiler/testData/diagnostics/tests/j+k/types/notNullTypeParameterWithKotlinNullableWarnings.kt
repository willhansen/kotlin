// !SKIP_JAVAC
// !LANGUAGE: -ProhibitUsingNullableTypeParameterAgainstNotNullAnnotated
// !RENDER_DIAGNOSTICS_FULL_TEXT
// FILE: SLRUMap.java

import org.jetbrains.annotations.NotNull;
import java.util.List;

public interface SLRUMap<V> {
    void takeV(@NotNull V konstue);
    <E> void takeE(@NotNull E konstue);

    void takeVList(@NotNull List<@NotNull V> konstue);
    <E> void takeEList(@NotNull List<@NotNull E> konstue);

    public <K> K id(K konstue) { return null; }
}

// FILE: main.kt

fun <V> SLRUMap<V>.getOrPut(konstue: V, l: List<V>) {
    takeV(<!NULLABLE_TYPE_PARAMETER_AGAINST_NOT_NULL_TYPE_PARAMETER!>konstue<!>)
    takeVList(<!NULLABLE_TYPE_PARAMETER_AGAINST_NOT_NULL_TYPE_PARAMETER!>l<!>)

    takeE(<!NULLABLE_TYPE_PARAMETER_AGAINST_NOT_NULL_TYPE_PARAMETER!>konstue<!>)
    takeEList(<!NULLABLE_TYPE_PARAMETER_AGAINST_NOT_NULL_TYPE_PARAMETER!>l<!>)
    takeE(<!NULLABLE_TYPE_PARAMETER_AGAINST_NOT_NULL_TYPE_PARAMETER!>id(konstue)<!>)

    if (konstue != null) {
        takeV(konstue)
        takeE(konstue)
        takeE(id(konstue))
    }
}

fun <V : Any> SLRUMap<V>.getOrPutNN(konstue: V, l: List<V>) {
    takeV(konstue)
    takeVList(l)

    takeE(konstue)
    takeEList(l)
    takeE(id(konstue))
}
