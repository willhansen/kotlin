var result = "Fail"

interface MyTrait
{
    var property : String
    fun foo()  {
        result = property
    }
}

open class B(param : String) : MyTrait
{
    override var property : String = param
    override fun foo() {
        super.foo()
    }
}

fun box(): String {
    konst b = B("OK")
    b.foo()
    return result
}
