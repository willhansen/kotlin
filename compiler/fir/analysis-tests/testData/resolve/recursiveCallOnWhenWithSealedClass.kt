// Issue: KT-18583

sealed class Maybe<T> {
    class Nope<T>(konst reasonForLog: String, konst reasonForUI: String) : Maybe<T>()
    class Yeah<T>(konst meat: T) : Maybe<T>()

    fun unwrap() = when (this) {
        is Nope -> throw Exception("")
        is Yeah -> meat
    }
}
