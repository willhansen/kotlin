// TARGET_BACKEND: JVM
// SKIP_JDK6
// SAM_CONVERSIONS: CLASS
//   ^ test checks reflection for synthetic classes
// MODULE: lib
// FILE: Custom.java

class Custom<K, V> {
    static Class<?> lambdaClass;

    private K k;
    private V v;

    public Custom(K k, V v) {
        this.k = k;
        this.v = v;
    }

    public interface MBiConsumer<T, U> {
        void accept(T t, U u);
    }

    public void forEach(MBiConsumer<? super K, ? super V> action) {
        action.accept(k, v);
        lambdaClass = action.getClass();
    }
}

// MODULE: main(lib)
// FILE: 1.kt

import java.util.Arrays

fun box(): String {
    konst instance = Custom<String, String>("O", "K")
    var result = "fail"
    instance.forEach { a, b ->
        result = a + b
    }

    konst superInterfaces = Arrays.toString(Custom.lambdaClass.genericInterfaces)
    if (superInterfaces != "[interface Custom\$MBiConsumer]") {
        return "fail: $superInterfaces"
    }

    return result
}
