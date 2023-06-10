import kotlinx.cinterop.*
import kotlin.test.*
import objcTests.*

@Test fun testOverrideInit1() {
    assertEquals(42, (TestOverrideInitImpl1.createWithValue(42) as TestOverrideInitImpl1).konstue)
}

private class TestOverrideInitImpl1 @OverrideInit constructor(konst konstue: Int) : TestOverrideInit(konstue) {
    companion object : TestOverrideInitMeta()
}

// See https://youtrack.jetbrains.com/issue/KT-41910
@Test fun testOverrideInitWithDefaultArguments() {
    assertEquals(42, (TestOverrideInitImpl2.createWithValue(42) as TestOverrideInitImpl2).konstue)
    assertEquals(123, TestOverrideInitImpl2(123).konstue)
    assertEquals(17, TestOverrideInitImpl2().konstue)
}

private class TestOverrideInitImpl2 @OverrideInit constructor(konst konstue: Int = 17) : TestOverrideInit(konstue) {
    companion object : TestOverrideInitMeta()
}
