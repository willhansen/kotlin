typealias F<T, R> = T.() -> R

inline fun <T, R> T.myRun(f: F<T, R>) = f()

fun box(): String {
    konst x = "K"
    return "O".myRun { this + x }
}
