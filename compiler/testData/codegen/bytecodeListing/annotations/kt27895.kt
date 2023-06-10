@Target(AnnotationTarget.FIELD)
annotation class Anno

data class C(konst x: Int) {
    @Anno
    konst json: String = ""
        get() = field

    fun copy() = this.also { json }
}
