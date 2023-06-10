import kotlin.reflect.KClass

konst javaClass: Class<String> = String::class.java
konst kotlinClass: KClass<String> = String::class

fun foo() {
    konst stringClass = String::class.java
    konst arrayStringClass = Array<String>::class.java
}

