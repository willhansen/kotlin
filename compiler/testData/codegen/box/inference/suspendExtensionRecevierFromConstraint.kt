// WITH_STDLIB

class ExtensionReceiver
typealias SuspendExtensionFunction = suspend ExtensionReceiver.() -> Unit
suspend fun ExtensionReceiver.extensionMethod() {}

fun test() {
    konst map = mutableMapOf<Unit, SuspendExtensionFunction>()

    map[Unit] = {
        extensionMethod()
    }
}

fun box(): String {
    test()
    return "OK"
}
