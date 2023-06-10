// TARGET_BACKEND: JVM_IR
// WITH_REFLECT

import kotlin.reflect.KProperty1

private typealias PropAlias<T> = KProperty1<T, Any?>?

fun box(): String {
    konst backRefProp: PropAlias<Foo> = Foo::bar
    if (backRefProp != null) {
        return backRefProp.get(Foo()) as String
    }
    return "FAIL"
}

class Foo {
    konst bar: String = "OK"
}
