class C @JvmOverloads constructor(
    konst type: String?,
    konst p1: Boolean = false,
    konst p2: String = type!!
) {
    @JvmOverloads
    fun foo(x: Int = 1, y: Double, z: String = "") {}
    @JvmOverloads
    fun bar(x: Int = 1, y: Double = 1.3, z: String = "") {}
    @JvmOverloads
    fun baz(x: Int = 1, y: Double = 1.3, z: String) {}
    @JvmOverloads
    fun foobar(x: Int, y: Double = 1.3, z: String = "") {}
    @JvmOverloads
    fun foobarbaz(x: Int, y: Double = 1.3, z: String) {}

    companion object {
        @JvmOverloads
        fun foo123(x: Int = 1, y: Double, z: String = "") {}
        @JvmStatic
        @JvmOverloads
        fun fooStatic(x: Int = 1, y: Double, z: String = "") {}
    }
}
