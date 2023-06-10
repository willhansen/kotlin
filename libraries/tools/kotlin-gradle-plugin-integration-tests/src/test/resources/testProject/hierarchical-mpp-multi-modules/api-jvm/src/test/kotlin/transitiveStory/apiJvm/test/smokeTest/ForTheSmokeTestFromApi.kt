package transitiveStory.apiJvm.test.smokeTest

import org.junit.Test
import transitiveStory.apiJvm.beginning.tlAPIkonst
import kotlin.test.assertEquals

class KClassForTheSmokeTestFromApi {
}

class SomeTestInApiJVM {
    @Test
    fun some() {
        println("I'm simple test in `api-jvm` module")
        assertEquals(tlAPIkonst, 42)
    }

    // KT-33573
    @Test
    fun `function with spaces`() {}
}
