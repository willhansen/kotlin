interface Box<T>

public fun <T> foo(nextFunction: (T) -> T): Box<T> = null!!

fun leaves(konstue: String, forward: Boolean): Box<String> {
    if (forward) {
        return foo { "" }
    } else {
        return foo { "" }
    }
}
