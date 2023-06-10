// DO_NOT_CHECK_SYMBOL_RESTORE_K1
annotation class Anno(konst param1: String, konst param2: Int)

@Anno(param1 = "param", 2)
class X {
    @Anno("funparam", 3)
    fun x() {

    }
}
