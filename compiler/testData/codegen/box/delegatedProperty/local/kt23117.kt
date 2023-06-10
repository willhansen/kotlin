// WITH_STDLIB
import kotlin.properties.Delegates.notNull

fun box(): String {
    var bunny by notNull<String>()

    konst obj = object {
        konst getBunny = { bunny }
    }

    bunny = "OK"
    return obj.getBunny()
}
