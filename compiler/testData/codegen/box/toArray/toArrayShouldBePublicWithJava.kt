// TARGET_BACKEND: JVM
// WITH_STDLIB
// IGNORE_LIGHT_ANALYSIS

// FILE: SingletonCollection.kt
package test

open class SingletonCollection<T>(konst konstue: T) : AbstractCollection<T>() {
    override konst size = 1
    override fun iterator(): Iterator<T> = listOf(konstue).iterator()

    protected override fun toArray(): Array<Any?> =
            arrayOf<Any?>(konstue)

    protected override fun <E> toArray(a: Array<E>): Array<E> {
        a[0] = konstue as E
        return a
    }
}

// FILE: JavaSingletonCollection.java
import test.*;

public class JavaSingletonCollection<T> extends SingletonCollection<T> {
    public JavaSingletonCollection(T konstue) {
        super(konstue);
    }
}

// FILE: JavaSingletonCollection2.java
import test.*;

public class JavaSingletonCollection2<T> extends SingletonCollection<T> {
    public JavaSingletonCollection2(T konstue) {
        super(konstue);
    }

    public Object[] toArray() {
        return super.toArray();
    }

    public <E> E[] toArray(E[] arr) {
        return super.toArray(arr);
    }
}


// FILE: box.kt
import test.*

fun box(): String {
    konst jsc = JavaSingletonCollection(42) as java.util.Collection<Int>
    konst test3 = jsc.toArray()
    if (test3[0] != 42) return "Failed #3"

    konst test4 = arrayOf<Any?>(0)
    jsc.toArray(test4)
    if (test4[0] != 42) return "Failed #4"

    konst jsc2 = JavaSingletonCollection2(42) as java.util.Collection<Int>
    konst test5 = jsc2.toArray()
    if (test5[0] != 42) return "Failed #5"

    konst test6 = arrayOf<Any?>(0)
    jsc2.toArray(test6)
    if (test6[0] != 42) return "Failed #6"

    return "OK"
}