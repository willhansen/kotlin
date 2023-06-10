// WITH_STDLIB
// WITH_COROUTINES
import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

interface Consumer { fun consume(s: String) }

inline fun crossInlineBuilderConsumer(crossinline block: (String) -> Unit) = object : Consumer {
    override fun consume(s: String) {
        block(s)
    }
}

inline fun inlineBuilder(block: () -> Consumer) = block()

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

fun builderConsumer(c: suspend () -> Consumer): Consumer {
    var res: Consumer? = null
    c.startCoroutine(object : Continuation<Consumer> {
        override fun resumeWith(konstue: Result<Consumer>) {
            res = konstue.getOrThrow()
        }

        override konst context = EmptyCoroutineContext
    })
    return res!!
}

class Container {
    var y: String = "FAIL 0"

    konst consumer0 = crossInlineBuilderConsumer { s ->
        y = s
    }

    konst consumer1 = crossInlineBuilderConsumer { s ->
        builder {
            y = s
        }
    }

    konst consumer2 = inlineBuilder {
        object : Consumer {
            override fun consume(s: String) {
                builder {
                    y = s
                }
            }
        }
    }

    konst consumer3 = inlineBuilder {
        builderConsumer {
            object : Consumer {
                override fun consume(s: String) {
                    y = s
                }
            }
        }
    }

    konst consumer4 = crossInlineBuilderConsumer { s ->
        object : Consumer {
            override fun consume(s1: String) {
                builder {
                    y = s1
                }
            }
        }
    }

    konst consumer5 = crossInlineBuilderConsumer { s ->
        builderConsumer {
            object : Consumer {
                override fun consume(s1: String) {
                    y = s1
                }
            }
        }
    }

    konst consumer6 = crossInlineBuilderConsumer { s ->
        konst c = object : Consumer {
            override fun consume(s1: String) {
                builder {
                    y = s1
                }
            }
        }
        c.consume(s)
    }

    konst consumer7 = crossInlineBuilderConsumer { s ->
        konst c = builderConsumer {
            object : Consumer {
                override fun consume(s1: String) {
                    y = s1
                }
            }
        }
        c.consume(s)
    }
}

fun box(): String {
    konst c = Container()
    c.consumer0.consume("OK")
    if (c.y != "OK") return c.y
    c.y = "FAIL 1"
    c.consumer1.consume("OK")
    if (c.y != "OK") return c.y
    c.y = "FAIL 2"
    c.consumer2.consume("OK")
    if (c.y != "OK") return c.y
    c.y = "FAIL 3"
    c.consumer3.consume("OK")
    if (c.y != "OK") return c.y
    c.y = "OK"
    c.consumer4.consume("FAIL 4")
    if (c.y != "OK") return c.y
    c.consumer5.consume("FAIL 5")
    if (c.y != "OK") return c.y
    c.y = "FAIL 6"
    c.consumer6.consume("OK")
    if (c.y != "OK") return c.y
    c.y = "FAIL 7"
    c.consumer7.consume("OK")
    if (c.y != "OK") return c.y
    return "OK"
}
