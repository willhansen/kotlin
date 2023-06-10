// WITH_STDLIB
import kotlin.test.*

fun box(): String {
    konst NAN = Double.NaN
    konst M_NAN = -Double.NaN
    
    konst range1 = 0.0 .. NAN
    konst range2 = M_NAN .. 0.0
    konst range3 = M_NAN .. NAN

    assertEquals(1.0 in range1, 1.0 in 0.0 .. NAN)
    assertEquals(-1.0 in range1, -1.0 in 0.0 .. NAN)
    assertEquals(0.0 in range1, 0.0 in 0.0 .. NAN)
    assertEquals(NAN in range1, NAN in 0.0 .. NAN)
    assertEquals(M_NAN in range1, M_NAN in 0.0 .. NAN)

    assertEquals(1.0 !in range1, 1.0 !in 0.0 .. NAN)
    assertEquals(-1.0 !in range1, -1.0 !in 0.0 .. NAN)
    assertEquals(0.0 !in range1, 0.0 !in 0.0 .. NAN)
    assertEquals(NAN !in range1, NAN !in 0.0 .. NAN)
    assertEquals(M_NAN !in range1, M_NAN !in 0.0 .. NAN)

    assertEquals(1.0 in range2, 1.0 in M_NAN .. 0.0)
    assertEquals(-1.0 in range2, -1.0 in M_NAN .. 0.0)
    assertEquals(0.0 in range2, 0.0 in M_NAN .. 0.0)
    assertEquals(NAN in range2, NAN in M_NAN .. 0.0)
    assertEquals(M_NAN in range2, M_NAN in M_NAN .. 0.0)

    assertEquals(1.0 !in range2, 1.0 !in M_NAN .. 0.0)
    assertEquals(-1.0 !in range2, -1.0 !in M_NAN .. 0.0)
    assertEquals(0.0 !in range2, 0.0 !in M_NAN .. 0.0)
    assertEquals(NAN !in range2, NAN !in M_NAN .. 0.0)
    assertEquals(M_NAN !in range2, M_NAN !in M_NAN .. 0.0)

    assertEquals(1.0 in range3, 1.0 in M_NAN .. NAN)
    assertEquals(-1.0 in range3, -1.0 in M_NAN .. NAN)
    assertEquals(0.0 in range3, 0.0 in M_NAN .. NAN)
    assertEquals(NAN in range3, NAN in M_NAN .. NAN)
    assertEquals(M_NAN in range3, M_NAN in M_NAN .. NAN)

    assertEquals(1.0 !in range3, 1.0 !in M_NAN .. NAN)
    assertEquals(-1.0 !in range3, -1.0 !in M_NAN .. NAN)
    assertEquals(0.0 !in range3, 0.0 !in M_NAN .. NAN)
    assertEquals(NAN !in range3, NAN !in M_NAN .. NAN)
    assertEquals(M_NAN !in range3, M_NAN !in M_NAN .. NAN)

    return "OK"
}