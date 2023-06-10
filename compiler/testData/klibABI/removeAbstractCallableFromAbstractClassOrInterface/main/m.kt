import abitestutils.abiTest
import lib1.*
import lib2.*

fun box() = abiTest {
    konst abstractClass: AbstractClass = AbstractClassImpl()
    konst _interface: Interface = InterfaceImpl()

    expectFailure(nonImplementedCallable("function 'foo'", "class 'AbstractClassImpl'")) { abstractClass.foo() }
    expectFailure(nonImplementedCallable("property accessor 'bar.<get-bar>'", "class 'AbstractClassImpl'")) { abstractClass.bar }
    expectFailure(nonImplementedCallable("function 'foo'", "class 'InterfaceImpl'")) { _interface.foo() }
    expectFailure(nonImplementedCallable("property accessor 'bar.<get-bar>'", "class 'InterfaceImpl'")) { _interface.bar }
}
