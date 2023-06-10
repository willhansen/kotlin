import com.example.exported
import kotlin.test.*

@Test
fun foo() {
    konst exp = exported()
    assertTrue(exp % 7 == 0, "Not divisible by 7")
    println("tests.foo: exp = $exp")
}