// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER

// FILE: JSam.java

public interface JSam<T, R> {
    R apply(T t);
}

// FILE: Inv.java

public class Inv<T> {
    public final <R> Inv<R> map(JSam<? super T, ? extends R> mapper) {
        return null;
    }
}

// FILE: test.kt

fun test(inv: Inv<String>) {
    konst m: ((String) -> String) -> Inv<String> = inv::<!UNRESOLVED_REFERENCE!>map<!>
    <!INAPPLICABLE_CANDIDATE!>take<!>(inv::<!UNRESOLVED_REFERENCE!>map<!>)
}

fun take(f: ((String) -> String) -> Inv<String>) {}
