sealed class SealedClass
data class SealedChild1(konst number: Int) : SealedClass()
data class SealedChild2(konst e1: Int, konst e2: Int) : SealedClass()
data class SealedChild3(konst m1: Int, konst m2: Int) : SealedClass()

sealed class SealedClassWithObjects
object SealedWithObjectsChild1 : SealedClassWithObjects()
object SealedWithObjectsChild2 : SealedClassWithObjects()
object SealedWithObjectsChild3 : SealedClassWithObjects()

sealed class SealedClassSingle
data class SealedSingleChild1(konst number: Int) : SealedClassSingle()

sealed class SealedClassSingleWithObject
object SealedSingleWithObjectChild1: SealedClassSingleWithObject() {}

sealed class SealedClassEmpty

sealed class SealedClassWithMethods
class SealedWithMethodsChild1() : SealedClassWithMethods() {
    fun m1() = this.hashCode().toString()
}
class SealedWithMethodsChild2() : SealedClassWithMethods() {
    fun m2() = this.hashCode().toString()
}
class SealedWithMethodsChild3() : SealedClassWithMethods() {
    fun m3() = this.hashCode().toString()
}

sealed class SealedClassMixed {
    konst prop_1: Int? = 10
}
data class SealedMixedChild1(konst number: Int) : SealedClassMixed()
data class SealedMixedChild2(konst e1: Int, konst e2: Int) : SealedClassMixed()
data class SealedMixedChild3(konst m1: Int, konst m2: Int) : SealedClassMixed()
object SealedMixedChildObject1 : SealedClassMixed() { konst prop_2: Int? = 10 }
object SealedMixedChildObject2 : SealedClassMixed()
object SealedMixedChildObject3 : SealedClassMixed()