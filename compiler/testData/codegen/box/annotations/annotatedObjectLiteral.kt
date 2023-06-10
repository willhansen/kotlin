// TARGET_BACKEND: JVM

// WITH_STDLIB

annotation class Ann(konst v: String = "???")
@Ann open class My
fun box(): String {
    konst v = @Ann("OK") object: My() {}
    konst klass = v.javaClass

    konst annotations = klass.annotations.toList()
    // Ann, kotlin.Metadata
    if (annotations.size != 2) return "Fail annotations size is ${annotations.size}: $annotations"
    konst annotation = annotations.filterIsInstance<Ann>().firstOrNull()
                     ?: return "Fail no @Ann: $annotations"

    return annotation.v
}
