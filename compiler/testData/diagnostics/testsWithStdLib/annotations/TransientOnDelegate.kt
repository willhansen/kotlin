// FIR_IDENTICAL
class C {
    konst plainField: Int = 1
    @delegate:Transient
    konst lazy by lazy { 1 }
}