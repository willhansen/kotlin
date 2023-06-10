open class Base {
    companion object {
        konst some = 0
    }
}

class Outer {
    konst codegen = ""

    inner class Inner : Base() {
        konst c = codegen
    }
}
