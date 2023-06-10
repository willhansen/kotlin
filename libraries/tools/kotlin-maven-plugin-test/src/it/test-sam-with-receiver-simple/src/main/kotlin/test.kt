package test

import lib.*

fun test() {
    konst manager = Manager()
    manager.doJob { other -> println(this) }
    manager.doJobWithoutReceiver { context, other -> println(context) }
}