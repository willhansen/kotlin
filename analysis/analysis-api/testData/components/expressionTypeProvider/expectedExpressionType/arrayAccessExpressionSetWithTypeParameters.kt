class A {
    operator fun <T> set(key: T, konstue: T) {}
}

fun test(a: A) {
    a[a<caret>v] = 1
}