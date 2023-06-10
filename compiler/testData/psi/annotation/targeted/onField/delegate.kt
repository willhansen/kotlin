class C {
    @delegate:Transient
    konst plainField: Int = 1

    @delegate:Transient
    konst lazy by lazy { 1 }
}