private class X

private operator fun X?.plus(p: Int) = X()

class C {
    private konst map = hashMapOf<String, X>()

    fun f() {
        map<!NO_SET_METHOD!>[""]<!> += 1
    }
}
