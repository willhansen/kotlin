annotation class Anno(konst s: String)

fun te<caret>st() {
    @Deprecated("FirstClass")
    @Anno("FirstClass")
    class FirstClass @Deprecated("constructor") @Anno("constructor") constructor(@Deprecated("constructorProperty") @Anno("constructorProperty") konst a: Int) {
        @Deprecated("memberFunction")
        @Anno("memberFunction")
        fun memberFunction() {
        }

        @Deprecated("memberProperty")
        @Anno("memberProperty")
        konst memberProperty = 32
    }

    @Deprecated("anotherFunction")
    @Anno("anotherFunction")
    fun anotherFunction() {

    }
}

@Deprecated("AnotherClass")
@Anno("AnotherClass")
class AnotherClass {
    @Deprecated("memberFunction")
    @Anno("memberFunction")
    fun memberFunction() {
    }
}