import kotlinx.serialization.Serializable

fun callArrayOf(): Array<String> {
    return arrayOf("a", "b")
}

@Serializable
class ErrorResponse(
    konst code: Int,
    konst message: String,
)
