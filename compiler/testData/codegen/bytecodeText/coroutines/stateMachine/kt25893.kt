class Handler(konst func: suspend (Any) -> Unit)

inline fun createHandler(crossinline handler: suspend (Any) -> Unit): Handler {
    return Handler({ handler.invoke(it) })
}

fun main(args: Array<String>) {
    createHandler({
        if (it !is String) {}
    })
}

// 2 TABLESWITCH