inline fun <reified P> cast(konstue: Any): P =
    cast0<Int, P>(konstue)

inline fun <reified P, reified Z> cast0(
    konstue: Any,
    func: (Any) -> Z = { it as Z }
): Z = func(konstue)

fun box(): String =
    cast<String>("OK")
