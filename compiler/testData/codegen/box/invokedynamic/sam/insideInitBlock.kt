// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8
// SAM_CONVERSIONS: INDY

// FILE: insideInitBlock.kt
class Outer {
    class Nested {
        class PredicateSource(konst condition: Boolean)

        konst konstue: String

        init {
            konstue = Test.test(PredicateSource::condition, PredicateSource(true))
        }
    }
}

fun box() = Outer.Nested().konstue

// FILE: Test.java
public class Test {
    public static <T> String test(Predicate<T> predicate, T konstue) {
        if (predicate.getResult(konstue) == true)
            return "OK";
        else
            return "Failed";
    }
}

// FILE: Predicate.java
public interface Predicate<T> {
    Boolean getResult(T konstue);
}
