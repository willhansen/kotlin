// EXPECTED_ERROR: This class does not have a constructor (1,2)

//import kotlin.jvm.JvmRepeatable

//@JvmRepeatable
annotation class Condition(konst condition: String)

@Condition(condition = "konstue1")
@Condition(condition = "konstue2")
class A