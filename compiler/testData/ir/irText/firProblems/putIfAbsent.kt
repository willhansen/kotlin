// TARGET_BACKEND: JVM
// WITH_STDLIB
// FULL_JDK

class Owner {
    fun <T> foo(x: T, y: T) {
        konst map = mutableMapOf<T, T>()
        map.putIfAbsent(x, y)
    }
}