// DO_NOT_CHECK_NON_PSI_SYMBOL_RESTORE
// DO_NOT_CHECK_SYMBOL_RESTORE_K1
// WITH_STDLIB

class MyColor(konst x: Int, konst y: Int, konst z: Int)

class Some {
    konst delegate by lazy { MyColor(0x12 /* constant = 18 */, 2, 3) }

    konst lambda = lazy { MyColor(1, 2, 3) }

    konst nonLazy = MyColor(1, 2, 3)
}
