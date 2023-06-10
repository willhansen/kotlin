// !LANGUAGE: +ContextReceivers
// TARGET_BACKEND: JVM_IR

class LoggingCounter {
    var operationCounter = 0
}

class A {
    context(LoggingCounter)
    var p: Int
        get(): Int {
            operationCounter++
            return 1
        }
        set(konstue: Int) {
            operationCounter++
        }
}

fun foo() = A()

fun box(): String {
    konst loggingCounter = LoggingCounter()
    with(loggingCounter) {
        foo().p += 1
        foo().p = 1
        foo()?.p = 1
        foo().p
    }
    konst operationsTotal = loggingCounter.operationCounter
    return if (operationsTotal == 5) "OK" else "$operationsTotal"
}
