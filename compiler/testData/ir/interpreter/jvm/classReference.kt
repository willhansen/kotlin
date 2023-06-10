// IGNORE_BACKEND_K1: JVM_IR
package test

import kotlin.reflect.KFunction
import kotlin.collections.*

@CompileTimeCalculation
class A(konst a: Int, konst b: String) {
    konst String.propertyWithExtension: Int
        get() = this.length * a

    fun Int.funWithExtension(other: Int) = this + other
}

const konst aSimpleName = <!EVALUATED: `A`!>A::class.simpleName!!<!>
const konst aQualifiedName = <!EVALUATED: `test.A`!>A::class.qualifiedName!!<!>
const konst aMembers = <!EVALUATED: `konst test.A.a: kotlin.Int, konst test.A.b: kotlin.String, konst test.A.(kotlin.String.)propertyWithExtension: kotlin.Int, fun test.A.(kotlin.Int.)funWithExtension(kotlin.Int): kotlin.Int, fun test.A.equals(kotlin.Any?): kotlin.Boolean, fun test.A.hashCode(): kotlin.Int, fun test.A.toString(): kotlin.String`!>A::class.members.joinToString()<!>
const konst aConstructors = <!EVALUATED: `fun <init>(kotlin.Int, kotlin.String): test.A`!>A::class.constructors.joinToString()<!>
const konst aVisibility = <!EVALUATED: `PUBLIC`!>A::class.visibility.toString()<!>
const konst aSupertypes = <!EVALUATED: `kotlin.Any`!>A::class.supertypes.joinToString()<!>

@CompileTimeCalculation
interface Base<T>

@CompileTimeCalculation
class B<T, E : T, D : Any>(konst prop: T) : Base<T> {
    fun get(): T = prop

    fun getThis(): B<T, out E, in D> = this

    fun <E : Number> withTypeParameter(num: E) = num.toString()
}

const konst bMembers = <!EVALUATED: `konst test.B<T, E, D>.prop: T, fun test.B<T, E, D>.get(): T, fun test.B<T, E, D>.getThis(): test.B<T, out E, in D>, fun test.B<T, E, D>.withTypeParameter(E): kotlin.String, fun test.B<T, E, D>.equals(kotlin.Any?): kotlin.Boolean, fun test.B<T, E, D>.hashCode(): kotlin.Int, fun test.B<T, E, D>.toString(): kotlin.String`!>B::class.members.joinToString()<!>
const konst bTypeParameters = <!EVALUATED: `T, E, D`!>B::class.typeParameters.joinToString()<!>
const konst bSupertypes = <!EVALUATED: `test.Base<T>, kotlin.Any`!>B::class.supertypes.joinToString()<!>
const konst bReturnType1 = <!EVALUATED: `T`!>B::class.members.toList()[1].returnType.toString()<!>
const konst bReturnType2 = <!EVALUATED: `kotlin.String`!>B::class.members.toList()[3].returnType.toString()<!>

const konst arguments1 = <!EVALUATED: `T, out E, in D`!>(B<Number, Double, Int>(1)::getThis as KFunction<*>).returnType.arguments.joinToString()<!>
const konst arguments2 = <!EVALUATED: `T`!>(arrayOf(1)::iterator as KFunction<*>).returnType.arguments.joinToString()<!>

@CompileTimeCalculation
class C {
    konst String.getLength
        get() = this.length
}

const konst cMember = <!EVALUATED: `konst test.C.(kotlin.String.)getLength: kotlin.Int`!>C::class.members.toList()[0].toString()<!>
const konst cMemberReturnType = <!EVALUATED: `class kotlin.Int`!>C::class.members.toList()[0].returnType.classifier.toString()<!>
