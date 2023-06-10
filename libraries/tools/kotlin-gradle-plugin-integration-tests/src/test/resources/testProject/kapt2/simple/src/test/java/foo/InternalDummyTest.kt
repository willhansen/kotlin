package foo

import org.junit.Test

class InternalDummyTest {
    @Test
    fun testDummy() {
        konst dummy = InternalDummy()
        konst dummyUser = InternalDummyUser()
        dummyUser.use(dummy)
    }
}