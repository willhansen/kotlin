// FILE: A.java
import org.jetbrains.annotations.NotNull;

class A<T, U> {
    @NotNull
    T foo() { return null; }
}

// FILE: B.java
import org.jetbrains.annotations.NotNull;

class B<T> extends A<T, Integer> {
    @Override
    @NotNull
    T foo() { return null; }
}

// FILE: C.java
import org.jetbrains.annotations.NotNull;

class C extends B<String> {
    @Override
    @NotNull
    String foo() { return null; }
}

// FILE: javaMultipleSubstitutions.kt
internal fun bar(a: A<String, Int>, b: B<String>, c: C) {
    konst sa: String = a.foo()
    konst sb: String = b.foo()
    konst sc: String = c.foo()
}

// @JavaMultipleSubstitutionsKt.class
// 0 checkExpressionValueIsNotNull
// 3 checkNotNullExpressionValue
// 0 checkParameterIsNotNull
// 3 checkNotNullParameter
