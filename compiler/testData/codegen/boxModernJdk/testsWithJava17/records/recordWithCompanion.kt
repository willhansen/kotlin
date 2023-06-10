// ISSUE: KT-55703
// DUMP_IR

@JvmRecord
data class Tag(
    konst id: String,
) {
    companion object
}

fun box(): String {
    return Tag("OK").id
}
