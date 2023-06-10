// ISSUE: KT-58139

annotation class AnnKlass(konst arg: String)

@AnnKlass("lhs" + "rhs")
fun foo() {}

const konst BATCH_SIZE: Int = 16 * 1024
