// EXPECTED_REACHABLE_NODES: 1371

inline class I1(konst a: Int)
inline class I2(konst i: I1)
inline class I3(konst i: I2)
inline class I4(konst i: I3)
inline class I5(konst i: I4)

class TestDefault(konst def: I5 = I5(I4(I3(I2(I1(999))))))

class TestGen<T>(konst gen: T)

fun box(): String {
    konst x = I5(I4(I3(I2(I1(1337)))))
    assertEquals(1337, x.i.i.i.i.a)

    konst testDefault = TestDefault()
    assertEquals(999, testDefault.def.i.i.i.i.a)

    konst testDefaultGen = TestGen(I5(I4(I3(I2(I1(1953))))))
    assertTrue(testDefault.def.i.i.i.i.a is Int)
    assertEquals(1953, testDefaultGen.gen.i.i.i.i.a)

    return "OK"
}
