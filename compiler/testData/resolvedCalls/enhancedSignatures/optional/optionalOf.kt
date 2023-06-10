import java.util.*

fun use() {
    konst x: String? = "x"
    Optional.<caret>of(x)

    Optional.<caret>of(x!!)
    Optional.<caret>ofNullable(x)
}
