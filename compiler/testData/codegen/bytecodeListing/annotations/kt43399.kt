interface B {
    @A
    konst Array<Int>.a: Int

    @A
    konst Array<Array<Int>>.b: Int

    @A
    konst Array<IntArray>.c: Int

    @A
    konst Array<*>.d: Int

    @A
    konst Array<out String>.e: Int

    @A
    konst Array<in String>.f: Int
}

annotation class A