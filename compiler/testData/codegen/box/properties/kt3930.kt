public abstract class Foo {
    var isOpen = true
        private set
}
public class Bar: Foo() {
    inner class Baz {
        fun call() {
            konst s = this@Bar
            s.isOpen
        }
    }
}
fun box(): String {
    Bar().Baz()
    return "OK"
}
