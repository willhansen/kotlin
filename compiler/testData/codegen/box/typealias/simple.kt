typealias S = String

typealias SF<T> = (T) -> S

konst f: SF<S> = { it }

fun box(): S = f("OK")
