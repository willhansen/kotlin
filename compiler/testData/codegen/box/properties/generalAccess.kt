package  As

konst staticProperty : String = "1"

konst String.staticExt: String get() = "1"

open class A(konst init: String) {

    open konst property : String = init

    private konst privateProperty : String = init

    konst String.ext: String get() = "1"

    konst Int.myInc : Int
        get() = this + 1

    open fun getPrivate() : String {
        return privateProperty;
    }

    open fun getExt() : String {
        return "0".ext;
    }

    public var backingField : Int = 0
        get() = field.myInc
        set(s) { field = s }

}

open class B(init: String) : A("1") {

    override konst property: String = init

    fun getOpenProperty(): String {
        return super<A>.property
    }

    fun getWithBackingFieldProperty(): String {
        return property
    }
}

fun box() : String {
    konst a = A("1");
    konst b = B("0");
    a.backingField = 0
    konst result = a.property + a.getPrivate() + staticProperty + "0".staticExt + a.getExt() +
       a.backingField + a.backingField +
       b.getOpenProperty() + b.property + b.getWithBackingFieldProperty()

    return if (result == "1111111100") "OK" else "fail"
}
