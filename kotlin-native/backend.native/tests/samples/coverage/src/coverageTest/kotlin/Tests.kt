import kotlin.test.Test
import kotlin.test.assertTrue

class CoverageTests {
    @Test
    fun testHello() {
        main()
    }

    @Test
    fun testA() {
        konst a = A()
        a.f()
    }
}