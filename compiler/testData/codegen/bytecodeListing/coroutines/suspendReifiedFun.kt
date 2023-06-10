// WITH_STDLIB

open class AbstractStuff() {
    inline suspend fun<reified T> hello(konstue: T): T = println("Hello, ${T::class}").let { konstue }
}

class Stuff: AbstractStuff() {
    suspend fun foo() = hello(40)
}
