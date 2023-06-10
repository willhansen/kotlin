interface InterfaceToAbstractClass
interface InterfaceToOpenClass
interface InterfaceToFinalClass
interface InterfaceToAnnotationClass
interface InterfaceToObject
interface InterfaceToEnumClass
interface InterfaceToValueClass
interface InterfaceToDataClass

open class OpenClassToFinalClass(konst x: Int)
open class OpenClassToAnnotationClass(konst x: Int)
open class OpenClassToObject(konst x: Int)
open class OpenClassToEnumClass(konst x: Int)
open class OpenClassToValueClass(konst x: Int)
open class OpenClassToDataClass(konst x: Int)
open class OpenClassToInterface(konst x: Int)

interface InterfaceToAbstractClass1
interface InterfaceToAbstractClass2
abstract class AbstractClass

interface RemovedInterface {
    fun abstractFun(): String
    fun abstractFunWithDefaultImpl(): String = "RemovedInterface.abstractFunWithDefaultImpl"
    konst abstractVal: String
    konst abstractValWithDefaultImpl: String get() = "RemovedInterface.abstractValWithDefaultImpl"
}

abstract class RemovedAbstractClass {
    abstract fun abstractFun(): String
    open fun openFun(): String = "RemovedAbstractClass.openFun"
    fun finalFun(): String = "RemovedAbstractClass.finalFun"
    abstract konst abstractVal: String
    open konst openVal: String get() = "RemovedAbstractClass.openVal"
    konst finalVal: String get() = "RemovedAbstractClass.finalVal"
}

open class RemovedOpenClass {
    open fun openFun(): String = "RemovedOpenClass.openFun"
    fun finalFun(): String = "RemovedOpenClass.finalFun"
    open konst openVal: String get() = "RemovedOpenClass.openVal"
    konst finalVal: String get() = "RemovedOpenClass.finalVal"
}

abstract class AbstractClassWithChangedConstructorSignature(name: String) {
    konst greeting = "Hello, $name!"
}

open class OpenClassWithChangedConstructorSignature(name: String) {
    konst greeting = "Hello, $name!"
}

open class SuperSuperClass {
    open fun inheritsFrom() = "SuperSuperClass -> Any"
}
open class SuperClass : SuperSuperClass() {
    override fun inheritsFrom() = "SuperClass -> " + super.inheritsFrom()
}
class SuperSuperClassReplacedBySuperClass : SuperSuperClass() {
    override fun inheritsFrom() = "SuperSuperClassReplacedBySuperClass -> " + super.inheritsFrom()
}
class SuperClassReplacedBySuperSuperClass : SuperClass() {
    override fun inheritsFrom() = "SuperClassReplacedBySuperSuperClass -> " + super.inheritsFrom()
}
