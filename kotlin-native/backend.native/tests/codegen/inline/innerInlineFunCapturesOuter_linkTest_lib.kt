package a

class E(konst x: String) {
    inner class Inner {
        inline fun foo(y: String) = x + y
    }
}