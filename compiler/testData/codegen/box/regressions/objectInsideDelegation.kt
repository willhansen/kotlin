// WITH_STDLIB

konst b: First by lazy {
    object : First {   }
}

private konst withoutType by lazy {
    object : First { }
}

private konst withTwoSupertypes by lazy {
    object : First, Second { }
}

interface First
interface Second

fun box(): String {
    b
    withoutType
    withTwoSupertypes
    return "OK"
}