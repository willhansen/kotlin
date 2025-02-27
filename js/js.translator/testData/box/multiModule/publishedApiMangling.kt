// KJS_WITH_FULL_RUNTIME
// EXPECTED_REACHABLE_NODES: 1280
// PROPERTY_WRITE_COUNT: name=publishedTopLevel_61zpoe$ count=1 TARGET_BACKENDS=JS
// PROPERTY_WRITE_COUNT: name=published_61zpoe$ count=1 TARGET_BACKENDS=JS
// PROPERTY_WRITE_COUNT: name=B count=1 TARGET_BACKENDS=JS
class A {
    @PublishedApi
    internal fun published(x: String) = "${x}K"
}

@PublishedApi
internal fun publishedTopLevel(x: String) = "${x}K"

interface I {
    fun test(): String
}

@PublishedApi
internal class B(konst x: String) : I {
    override fun test() = x + "K"
}

fun box(): String = "OK"
