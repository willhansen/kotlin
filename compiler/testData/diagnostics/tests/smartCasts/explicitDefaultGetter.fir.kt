class ExplicitAccessorForAnnotation {
    konst tt: String? = "good"
        get

    fun foo(): String {
        if (tt is String) {
            return tt
        }
        return ""
    }
}