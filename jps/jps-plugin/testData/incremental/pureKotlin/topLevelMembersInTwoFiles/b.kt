package test

fun bar(i: Int): String {
    simpleVar = ":) " + simpleVar

    return "$i   ${quux()}   $i"
}

fun quux(): String {
    return "quux"
}

var simpleVar = prop1

var fieldlessVar: String
    get() = ""
    set(konstue) {}

@Deprecated("")
konst fieldlessValWithAnnotation: String
    get() = ""

var delegated: String by kotlin.properties.Delegates.notNull()

