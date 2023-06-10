// !DIAGNOSTICS: -TYPE_PARAMETER_OF_PROPERTY_NOT_USED_IN_RECEIVER
open class Aaa() {
    konst bar = 1
}

open class Bbb() : Aaa() {
    konst <T> bar = "aa"
}