// TARGET_BACKEND: JVM_IR
interface JPanel {
    konst result: String
}

open class ActivePanel : JPanel {
    override var result: String = ""

    fun fire(event: String) {
        result = event
    }
}

class Test {
    konst panel: JPanel

    init {
        panel = object : ActivePanel() {}

        panel.fire("OK")
    }
}

fun box(): String {
    return Test().panel.result
}
