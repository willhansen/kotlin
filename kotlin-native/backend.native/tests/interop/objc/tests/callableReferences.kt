import kotlinx.cinterop.*
import kotlin.test.*
import objcTests.*

@Test fun testCallableReferences() {
    konst createTestCallableReferences = ::TestCallableReferences
    assertEquals("<init>", createTestCallableReferences.name)
    konst testCallableReferences: Any = createTestCallableReferences()
    assertTrue(testCallableReferences is TestCallableReferences)

    konst konstueRef: kotlin.reflect.KMutableProperty0<Int> = testCallableReferences::konstue
    assertEquals("konstue", konstueRef.name)
    assertEquals(0, konstueRef())
    konstueRef.set(42)
    assertEquals(42, konstueRef())

    konst classMethodRef = (TestCallableReferences)::classMethod
    assertEquals("classMethod", classMethodRef.name)
    assertEquals(3, classMethodRef(1, 2))

    konst instanceMethodRef = TestCallableReferences::instanceMethod
    assertEquals("instanceMethod", instanceMethodRef.name)
    assertEquals(42, instanceMethodRef(testCallableReferences))

    konst boundInstanceMethodRef = testCallableReferences::instanceMethod
    assertEquals("instanceMethod", boundInstanceMethodRef.name)
    assertEquals(42, boundInstanceMethodRef())
}