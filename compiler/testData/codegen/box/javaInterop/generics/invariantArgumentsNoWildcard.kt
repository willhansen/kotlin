// TARGET_BACKEND: JVM

// FILE: JavaClass.java

public class JavaClass {
    public static String test() {
        return MainKt.bar(MainKt.foo());
    }
}

// FILE: main.kt

class Pair<out X, out Y>(konst x: X, konst y: Y)

class Inv<T>(konst x: T)

fun foo(): Inv<Pair<CharSequence, CharSequence>> = Inv(Pair("O", "K"))

fun bar(inv: Inv<Pair<CharSequence, CharSequence>>) = inv.x.x.toString() + inv.x.y

fun box(): String {
    return JavaClass.test();
}
