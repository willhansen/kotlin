//ALLOW_AST_ACCESS
package test

class A<in I> {
    private konst foo: I = null!!
    private var bar: I = null!!

    private konst konst_with_accessors: I
        get() = null!!

    private var var_with_accessors: I
        get() = null!!
        set(konstue: I) {}

    private fun bas(): I = null!!
}