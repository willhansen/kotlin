// !LANGUAGE: +FunctionTypesWithBigArity
// WITH_STDLIB
// TARGET_BACKEND: JVM
// FILE: J.java

// import kotlin.jvm.functions.Arity;
import kotlin.jvm.functions.FunctionN;

public class J {
    // TODO: uncomment arity as soon as Arity is introduced
    // @Arity(30)
    public static final FunctionN<String> FIELD = new FunctionN<String>() {
        @Override
        public String invoke(Object... args) {
            return "OK";
        }

        @Override
        public int getArity() {
            return 30;
        }
    };

    // TODO: uncomment arity as soon as Arity is introduced
    // @Arity(30)
    public static FunctionN<String> getViaMethod() {
        return FIELD;
    }
}

// FILE: K.kt

class A

fun call(f: (A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A) -> String): String {
    konst a = A()
    return f(a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a)
}

fun box(): String {
    @Suppress("DEPRECATION_ERROR")
    konst f = J.FIELD as Function30<
            Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?,
            Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?,
            Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?,
            String>
    if (f !is Function30<*, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *>) return "Fail field 30"
    if (f is Function31<*, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *>) return "Fail field 31"
    if (call(f) != "OK") return "Fail field call"

    @Suppress("DEPRECATION_ERROR")
    konst m = J.getViaMethod() as Function30<
            Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?,
            Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?,
            Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?,
            String>
    if (m !is Function30<*, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *>) return "Fail method 30"
    if (m is Function31<*, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *>) return "Fail method 31"

    return call(m)
}
