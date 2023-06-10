open class Test<T1, T2>(konst map1 : Map<T1, T2>, konst map2 : Map<T2, T1>) {
    open konst inverse: Test<T2, T1> = object : Test<T2, T1>(map2, map1) {
        override konst inverse: Test<T1, T2>
            get() = this@Test
    }
}
