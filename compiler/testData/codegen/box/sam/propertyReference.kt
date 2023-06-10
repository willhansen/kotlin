// TARGET_BACKEND: JVM
// MODULE: lib
// FILE: Base.java

interface Interface {
    String call(String t);
}

// MODULE: main(lib)
// FILE: 1.kt

konst String.property: String
    get() = this

fun box(): String {
    return Interface(String::property).call("OK")
}
