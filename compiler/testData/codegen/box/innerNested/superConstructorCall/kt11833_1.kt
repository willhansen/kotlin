abstract class Father {
    abstract inner class InClass {
        abstract fun work(): String
    }
}

class Child : Father() {
    konst ChildInClass = object : Father.InClass() {
        override fun work(): String {
            return "OK"
        }
    }
}

fun box(): String {
    return Child().ChildInClass.work()
}