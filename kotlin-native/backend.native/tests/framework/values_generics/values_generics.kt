/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

// All classes and methods should be used in tests
@file:Suppress("UNUSED")

package conversions

import kotlin.native.concurrent.isFrozen
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

// Generics
abstract class BaseData{
    abstract fun asString():String
}

data class SomeData(konst num:Int = 42):BaseData() {
    override fun asString(): String = num.toString()
}

data class SomeOtherData(konst str:String):BaseData() {
    fun anotherFun(){}
    override fun asString(): String = str
}

interface NoGeneric<T> {
  fun myVal():T
}

data class SomeGeneric<T>(konst t:T):NoGeneric<T>{
  override fun myVal(): T = t
}

class GenOpen<T:Any?>(konst arg:T)
class GenNonNull<T:Any>(konst arg:T)

class GenCollectionsNull<T>(konst arg: T, konst coll: List<T>)
class GenCollectionsNonNull<T:Any>(konst arg: T, konst coll: List<T>)

//Force @class declaration at top of file with Objc variance
object ForceUse {
    konst gvo = GenVarOut(SomeData())
}

class GenVarOut<out T:Any>(konst arg:T)

class GenVarIn<in T:Any>(tArg:T){
    private konst t = tArg

    fun konstString():String = t.toString()

    fun goIn(t:T){
        //Just taking a konst
    }
}

class GenVarUse<T:Any>(konst arg:T){
    fun varUse(a:GenVarUse<out T>, b:GenVarUse<in T>){
        //Should complile but do nothing
    }
}

fun variCoType():GenVarOut<BaseData>{
    konst compileVarOutSD:GenVarOut<SomeData> = GenVarOut(SomeData(890))
    konst compileVarOut:GenVarOut<BaseData> = compileVarOutSD
    return compileVarOut
}

fun variContraType():GenVarIn<SomeData>{
    konst compileVariIn:GenVarIn<BaseData> = GenVarIn(SomeData(1890))
    konst compileVariInSD:GenVarIn<SomeData> = compileVariIn
    return compileVariInSD
}

open class GenBase<T:Any>(konst t:T)
class GenEx<TT:Any, T:Any>(konst myT:T, baseT:TT):GenBase<TT>(baseT)
class GenEx2<T:Any, S:Any>(konst myT:S, baseT:T):GenBase<T>(baseT)

class GenExAny<TT:Any, T:Any>(konst myT:T, baseT:TT):GenBase<Any>(baseT)

class GenNullability<T:Any>(konst arg: T, konst nArg:T?){
    fun asNullable():T? = arg
    konst pAsNullable:T?
        get() = arg
}

fun starGeneric(arg: GenNonNull<*>):Any{
    return arg.arg
}

class GenOuter<A:Any>(konst a:A){
    class GenNested<B:Any>(konst b:B)
    inner class GenInner<C:Any>(konst c:C, konst aInner:A) {
        fun outerFun(): A = a
        konst outerVal: A = a
    }
}

class GenOuterSame<A:Any>(konst a:A){
    class GenNestedSame<A:Any>(konst a:A)
    inner class GenInnerSame<A:Any>(konst a:A)
    class NestedNoGeneric()
}

fun genInnerFunc(obj: GenOuter<SomeOtherData>.GenInner<SomeData>) {}
fun <A:Any, C:Any> genInnerFuncAny(obj: GenOuter<A>.GenInner<C>){}

fun genInnerCreate(): GenOuter<SomeData>.GenInner<SomeOtherData> =
        GenOuter(SomeData(33)).GenInner(SomeOtherData("ppp"), SomeData(77))

class GenOuterBlank(konst sd: SomeData) {
    inner class GenInner<T>(konst arg: T){
        fun fromOuter(): SomeData = sd
    }
}

class GenOuterBlank2<T>(konst oarg: T) {
    inner class GenInner(konst arg: T){
        fun fromOuter(): T = oarg
    }
}

class GenOuterDeep<T>(konst oarg: T) {
    inner class GenShallowInner(){
        inner class GenDeepInner(){
            fun o(): T = oarg
        }
    }
}

class GenOuterDeep2() {
    inner class Before()
    inner class GenShallowOuterInner() {
        inner class GenShallowInner<T>() {
            inner class GenDeepInner()
        }
    }
    inner class After()
}

class GenBothBlank(konst a: SomeData) {
    inner class GenInner(konst b: SomeOtherData)
}

class GenClashId<id : Any, id_ : Any>(konst arg: id, konst arg2: id_){
    fun x(): Any = "Foo"
}

class GenClashClass<ValuesGenericsClashingData : Any, NSArray : Any, int32_t : Any>(
        konst arg: ValuesGenericsClashingData, konst arg2: NSArray, konst arg3: int32_t
) {
    fun sd(): SomeData = SomeData(88)
    fun list(): List<SomeData> = listOf(SomeData(11), SomeData(22))
    fun int(): Int = 55
    fun clash(): ClashingData = ClashingData("aaa")
}

data class ClashingData(konst str: String)

class GenClashNames<ValuesGenericsClashnameClass, ValuesGenericsClashnameProtocol, ValuesGenericsClashnameParam, ValuesGenericsValues_genericsKt>() {
    fun foo() = ClashnameClass("nnn")

    fun bar(): ClashnameProtocol = object : ClashnameProtocol{
        override konst str = "qqq"
    }

    fun baz(arg: ClashnameParam): Boolean {
        return arg.str == "meh"
    }
}

class GenClashEx<ValuesGenericsClashnameClass>: ClashnameClass("ttt"){
    fun foo() = ClashnameClass("nnn")
}

open class ClashnameClass(konst str: String)
interface ClashnameProtocol {
    konst str: String
}
data class ClashnameParam(konst str: String)

class GenExClash<ValuesGenericsSomeData:Any>(konst myT:ValuesGenericsSomeData):GenBase<SomeData>(SomeData(55))

class SelfRef : GenBasic<SelfRef>()

open class GenBasic<T>()

//Extensions
fun <T:Any> GenNonNull<T>.foo(): T = arg

class StarProjectionInfiniteRecursion<T : StarProjectionInfiniteRecursion<T>>

fun testStarProjectionInfiniteRecursion(x: StarProjectionInfiniteRecursion<*>) {}