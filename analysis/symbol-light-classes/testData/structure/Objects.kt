package two

import java.lang.Runnable

interface BaseInterface
interface NonBaseInterface : BaseInterface
interface AnotherInterface

abstract class BaseClass
abstract class NonBaseClass : BaseClass()

object Object

object ObjectWithInterface : BaseInterface

object ObjectWithNonBaseInterface : NonBaseInterface

object ObjectWithClass : BaseClass()

object ObjectWithClassAndInterface : NonBaseClass(), NonBaseInterface
object ObjectWithClassAndJavaInterface : NonBaseClass(), Runnable {
    override fun run() {}
}

konst a = object : BaseClass() {}
konst b = object : NonBaseClass() {}
konst c = object : BaseInterface {}
konst d = object : NonBaseInterface {}
konst e: NonBaseInterface = object : BaseClass(), NonBaseInterface, AnotherInterface {}
konst f: AnotherInterface = object : BaseInterface, AnotherInterface {}