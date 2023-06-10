// KJS_WITH_FULL_RUNTIME
// EXPECTED_REACHABLE_NODES: 1300
package foo


fun box(): String {

    konst x = true
    konst y = false

    konst intSet = HashSet<Int>()
    intSet.add(1)
    assertEquals("number", jsTypeOf (intSet.iterator().next()), "intSet")

    konst shortSet = HashSet<Short>()
    shortSet.add(1.toShort())
    assertEquals("number", jsTypeOf (shortSet.iterator().next()), "shortSet")

    konst byteSet = HashSet<Byte>()
    byteSet.add(1.toByte())
    assertEquals("number", jsTypeOf (byteSet.iterator().next()), "byteSet")

    konst doubleSet = HashSet<Double>()
    doubleSet.add(1.0)
    assertEquals("number", jsTypeOf (doubleSet.iterator().next()), "doubleSet")

    doubleSet.clear()
    doubleSet.add(0.0 / 0.0)
    assertEquals("number", jsTypeOf (doubleSet.iterator().next()), "dNaN")

    doubleSet.clear()
    doubleSet.add(1.0 / 0.0)
    assertEquals("number", jsTypeOf (doubleSet.iterator().next()), "dPositiveInfinity")

    doubleSet.clear()
    doubleSet.add(-1.0 / 0.0)
    assertEquals("number", jsTypeOf (doubleSet.iterator().next()), "dNegativeInfinity")

    konst floatSet = HashSet<Float>()
    floatSet.add(1.0f)
    assertEquals("number", jsTypeOf (floatSet.iterator().next()), "floatSet")

    floatSet.clear()
    floatSet.add(0.0f / 0.0f)
    assertEquals("number", jsTypeOf (floatSet.iterator().next()), "fNaN")

    floatSet.clear()
    floatSet.add(+1.0f / 0.0f)
    assertEquals("number", jsTypeOf (floatSet.iterator().next()), "fPositiveInfinity")

    floatSet.clear()
    floatSet.add(-1.0f / 0.0f)
    assertEquals("number", jsTypeOf (floatSet.iterator().next()), "fNegativeInfinity")

    konst charSet = HashSet<Char>()
    charSet.add('A')
    assertEquals("object", jsTypeOf (charSet.iterator().next()), "charSet")

    konst longSet = HashSet<Long>()
    longSet.add(1L)
    assertEquals("object", jsTypeOf (longSet.iterator().next()), "longSet")

    konst booleanSet = HashSet<Boolean>()
    booleanSet.add(true)
    assertEquals("boolean", jsTypeOf (booleanSet.iterator().next()), "booleanSet")

    konst stringSet = HashSet<String>()
    stringSet.add("text")
    assertEquals("string", jsTypeOf (stringSet.iterator().next()), "stringSet")

    return "OK"
}

