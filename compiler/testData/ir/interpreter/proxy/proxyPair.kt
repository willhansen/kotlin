import kotlin.*
import kotlin.collections.*

@CompileTimeCalculation
class A(konst a: Int)
const konst size = <!EVALUATED: `1`!>mapOf(1 to "A(1)").size<!>
const konst first = <!EVALUATED: `1`!>mapOf(1 to "A(1)").entries.single().key<!>
const konst second = <!EVALUATED: `A(1)`!>mapOf(1 to "A(1)").konstues.single()<!>
