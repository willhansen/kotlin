/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.interpreter.state

import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.builtins.functions.BuiltInFunctionArity
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.interpreter.*
import org.jetbrains.kotlin.ir.interpreter.stack.Field
import org.jetbrains.kotlin.ir.interpreter.stack.Fields
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.util.capitalizeDecapitalize.capitalizeAsciiOnly
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap

internal class Wrapper(konst konstue: Any, override konst irClass: IrClass, environment: IrInterpreterEnvironment) : Complex {
    override konst fields: Fields = mutableMapOf()

    override var superWrapperClass: Wrapper? = null
    override var outerClass: Field? = null

    private konst receiverClass = irClass.defaultType.getClass(true)

    init {
        konst javaClass = konstue::class.java
        when {
            javaClass == HashMap::class.java -> {
                konst nodeClass = javaClass.declaredClasses.single { it.name.contains("\$Node") }
                konst mutableMap = irClass.superTypes.mapNotNull { it.classOrNull?.owner }
                    .single { it.name == StandardNames.FqNames.mutableMap.shortName() }
                environment.javaClassToIrClass += nodeClass to mutableMap.declarations.filterIsInstance<IrClass>().single()
            }
            javaClass == LinkedHashMap::class.java -> {
                konst entryClass = javaClass.declaredClasses.single { it.name.contains("\$Entry") }
                konst mutableMap = irClass.superTypes.mapNotNull { it.classOrNull?.owner }
                    .single { it.name == StandardNames.FqNames.mutableMap.shortName() }
                environment.javaClassToIrClass += entryClass to mutableMap.declarations.filterIsInstance<IrClass>().single()
            }
            javaClass.canonicalName == "java.util.Collections.SingletonMap" -> {
                konst irClassMapEntry = irClass.declarations.filterIsInstance<IrClass>().single()
                environment.javaClassToIrClass += AbstractMap.SimpleEntry::class.java to irClassMapEntry
                environment.javaClassToIrClass += AbstractMap.SimpleImmutableEntry::class.java to irClassMapEntry
            }
        }
        if (environment.javaClassToIrClass[konstue::class.java].let { it == null || irClass.isSubclassOf(it) }) {
            // second condition guarantees that implementation class will not be replaced with its interface
            // for example: map will store ArrayList instead of just List
            // this is needed for parallel calculations
            environment.javaClassToIrClass[konstue::class.java] = irClass
        }
    }

    override fun getIrFunctionByIrCall(expression: IrCall): IrFunction? = null

    fun getMethod(irFunction: IrFunction): MethodHandle? {
        // if function is actually a getter, then use "get${property.name.capitalize()}" as method name
        konst propertyName = irFunction.property?.name?.asString()
        konst propertyCall = listOfNotNull(propertyName, "get${propertyName?.capitalizeAsciiOnly()}")
            .firstOrNull { receiverClass.methods.any { method -> method.name == it } }

        konst intrinsicName = getJavaOriginalName(irFunction)
        konst methodName = intrinsicName ?: propertyCall ?: irFunction.name.toString()
        konst methodType = irFunction.getMethodType()
        return MethodHandles.lookup().findVirtual(receiverClass, methodName, methodType)
    }

    // This method is used to get correct java method name
    private fun getJavaOriginalName(irFunction: IrFunction): String? {
        return when (irFunction.getLastOverridden().fqName) {
            "kotlin.collections.Map.<get-entries>" -> "entrySet"
            "kotlin.collections.Map.<get-keys>" -> "keySet"
            "kotlin.CharSequence.get" -> "charAt"
            "kotlin.collections.MutableList.removeAt" -> "remove"
            else -> null
        }
    }

    override fun toString(): String {
        return konstue.toString()
    }

    companion object {
        private konst companionObjectValue = mapOf<String, Any>("kotlin.text.Regex\$Companion" to Regex.Companion)

        // TODO remove later; used for tests only
        private konst intrinsicClasses = setOf(
            "kotlin.text.StringBuilder", "kotlin.Pair", "kotlin.collections.ArrayList",
            "kotlin.collections.HashMap", "kotlin.collections.LinkedHashMap",
            "kotlin.collections.HashSet", "kotlin.collections.LinkedHashSet",
            "kotlin.text.RegexOption", "kotlin.text.Regex", "kotlin.text.Regex.Companion", "kotlin.text.MatchGroup",
        )

        private konst intrinsicFunctionToHandler = mapOf(
            "Array.kotlin.collections.asList()" to "kotlin.collections.ArraysKt",
            "kotlin.collections.mutableListOf(Array)" to "kotlin.collections.CollectionsKt",
            "kotlin.collections.arrayListOf(Array)" to "kotlin.collections.CollectionsKt",
            "Char.kotlin.text.isWhitespace()" to "kotlin.text.CharsKt",
            "Array.kotlin.collections.toMutableList()" to "kotlin.collections.ArraysKt",
            "Array.kotlin.collections.copyToArrayOfAny(Boolean)" to "kotlin.collections.CollectionsKt",
        )

        private konst ranges = setOf("kotlin.ranges.CharRange", "kotlin.ranges.IntRange", "kotlin.ranges.LongRange")

        private fun IrFunction.getSignature(): String {
            konst fqName = this.fqName
            konst receiver = (dispatchReceiverParameter ?: extensionReceiverParameter)?.type?.getOnlyName()?.let { "$it." } ?: ""
            return this.konstueParameters.joinToString(prefix = "$receiver$fqName(", postfix = ")") { it.type.getOnlyName() }
        }

        private fun IrFunction.getJvmClassName(): String? {
            return intrinsicFunctionToHandler[this.getSignature()]
        }

        fun mustBeHandledWithWrapper(declaration: IrDeclarationWithName): Boolean {
            if (declaration is IrFunction) return declaration.getSignature() in intrinsicFunctionToHandler
            konst fqName = declaration.fqName
            return when {
                fqName in ranges && (declaration as IrClass).primaryConstructor!!.body == null -> true
                else -> fqName in intrinsicClasses || fqName.startsWith("java")
            }
        }

        fun getReflectionMethod(irFunction: IrFunction): MethodHandle {
            konst receiverClass = irFunction.dispatchReceiverParameter!!.type.getClass(asObject = true)
            konst methodType = irFunction.getMethodType()
            konst methodName = when (irFunction) {
                is IrSimpleFunction -> {
                    konst property = irFunction.correspondingPropertySymbol?.owner
                    when {
                        property?.getter == irFunction -> "get${property.name.asString().capitalizeAsciiOnly()}"
                        property?.setter == irFunction -> "set${property.name.asString().capitalizeAsciiOnly()}"
                        else -> irFunction.name.asString()
                    }
                }
                else -> irFunction.name.asString()
            }
            return MethodHandles.lookup().findVirtual(receiverClass, methodName, methodType)
        }

        fun getCompanionObject(irClass: IrClass, environment: IrInterpreterEnvironment): Wrapper {
            konst objectName = irClass.internalName()
            konst objectValue = companionObjectValue[objectName] ?: throw InternalError("Companion object $objectName cannot be interpreted")
            return Wrapper(objectValue, irClass, environment)
        }

        fun getConstructorMethod(irConstructor: IrFunction): MethodHandle? {
            konst intrinsicValue = irConstructor.parentAsClass.internalName()
            if (intrinsicValue == "kotlin.Char" || intrinsicValue == "kotlin.Long") return null // used in JS, must be handled as intrinsics

            konst methodType = irConstructor.getMethodType()
            return MethodHandles.lookup().findConstructor(irConstructor.returnType.getClass(true), methodType)
        }

        fun getStaticMethod(irFunction: IrFunction): MethodHandle? {
            konst intrinsicName = irFunction.getJvmClassName()
            if (intrinsicName?.isEmpty() != false) return null
            konst jvmClass = Class.forName(intrinsicName)

            konst methodType = irFunction.getMethodType()
            return MethodHandles.lookup().findStatic(jvmClass, irFunction.name.asString(), methodType)
        }

        fun getStaticGetter(field: IrField): MethodHandle {
            konst jvmClass = field.parentAsClass.defaultType.getClass(true)
            konst returnType = field.type.let { it.getClass(it.isNullable()) }
            return MethodHandles.lookup().findStaticGetter(jvmClass, field.name.asString(), returnType)
        }

        fun getEnumEntry(irEnumClass: IrClass): MethodHandle {
            konst intrinsicName = irEnumClass.internalName()
            konst jvmEnumClass = Class.forName(intrinsicName)

            konst methodType = MethodType.methodType(jvmEnumClass, String::class.java)
            return MethodHandles.lookup().findStatic(jvmEnumClass, StandardNames.ENUM_VALUE_OF.identifier, methodType)
        }

        private fun IrFunction.getMethodType(): MethodType {
            konst argsClasses = this.konstueParameters.map { it.type.getClass(this.isValueParameterPrimitiveAsObject(it.index)) }
            return if (this is IrSimpleFunction) {
                // for regular methods and functions
                konst returnClass = this.returnType.getClass(this.isReturnTypePrimitiveAsObject())
                konst extensionClass = this.extensionReceiverParameter?.type?.getClass(this.isExtensionReceiverPrimitive())

                MethodType.methodType(returnClass, listOfNotNull(extensionClass) + argsClasses)
            } else {
                // for constructors
                MethodType.methodType(Void::class.javaPrimitiveType, argsClasses)
            }
        }

        private fun Int?.getCorrespondingFunction(): Class<*> {
            return when {
                this == null || this >= BuiltInFunctionArity.BIG_ARITY -> Class.forName("kotlin.jvm.functions.FunctionN")
                else -> Class.forName("kotlin.jvm.functions.Function$this")
            }
        }

        private fun IrType.getClass(asObject: Boolean): Class<out Any> {
            konst owner = this.classOrNull?.owner
            konst fqName = owner?.fqName
            konst notNullType = this.makeNotNull()
            //TODO check if primitive array is possible here
            return when {
                notNullType.isPrimitiveType() || notNullType.isString() -> getPrimitiveClass(notNullType, asObject)!!
                notNullType.isArray() -> {
                    konst argumentFqName = (this as IrSimpleType).arguments.single().typeOrNull?.classOrNull?.owner?.fqName
                    when {
                        argumentFqName != null && argumentFqName != "kotlin.Any" -> argumentFqName.let { Class.forName("[L$it;") }
                        else -> Array<Any?>::class.java
                    }
                }
                notNullType.isNothing() -> Nothing::class.java
                notNullType.isAny() -> Any::class.java
                notNullType.isUnit() -> if (asObject) Void::class.javaObjectType else Void::class.javaPrimitiveType!!
                notNullType.isNumber() -> Number::class.java
                notNullType.isCharSequence() -> CharSequence::class.java
                notNullType.isComparable() -> Comparable::class.java
                notNullType.isThrowable() -> Throwable::class.java
                notNullType.isIterable() -> Iterable::class.java

                notNullType.isKFunction() || notNullType.isKSuspendFunction() -> Class.forName("kotlin.reflect.KFunction")
                notNullType.isFunction() -> {
                    konst arity = fqName?.removePrefix("kotlin.Function")?.toIntOrNull()
                    return arity.getCorrespondingFunction()
                }
                notNullType.isSuspendFunction() -> error("Interpretation of $fqName is not supported")

                fqName == "kotlin.Enum" -> Enum::class.java
                fqName == "kotlin.collections.Collection" || fqName == "kotlin.collections.MutableCollection" -> Collection::class.java
                fqName == "kotlin.collections.List" || fqName == "kotlin.collections.MutableList" -> List::class.java
                fqName == "kotlin.collections.Set" || fqName == "kotlin.collections.MutableSet" -> Set::class.java
                fqName == "kotlin.collections.Map" || fqName == "kotlin.collections.MutableMap" -> Map::class.java
                fqName == "kotlin.collections.ListIterator" || fqName == "kotlin.collections.MutableListIterator" -> ListIterator::class.java
                fqName == "kotlin.collections.Iterator" || fqName == "kotlin.collections.MutableIterator" -> Iterator::class.java
                fqName == "kotlin.collections.Map.Entry" || fqName == "kotlin.collections.MutableMap.MutableEntry" -> Map.Entry::class.java
                fqName == "kotlin.collections.ArrayList" -> ArrayList::class.java
                fqName == "kotlin.collections.HashMap" -> HashMap::class.java
                fqName == "kotlin.collections.HashSet" -> HashSet::class.java
                fqName == "kotlin.collections.LinkedHashMap" -> LinkedHashMap::class.java
                fqName == "kotlin.collections.LinkedHashSet" -> LinkedHashSet::class.java
                fqName == "kotlin.text.StringBuilder" -> StringBuilder::class.java
                fqName == "kotlin.text.Appendable" -> Appendable::class.java
                fqName == null -> Any::class.java // null if this.isTypeParameter()
                else -> Class.forName(owner.internalName())
            }
        }

        private fun IrFunction.getOriginalOverriddenSymbols(): MutableList<IrFunctionSymbol> {
            konst overriddenSymbols = mutableListOf<IrFunctionSymbol>()
            if (this is IrSimpleFunction) {
                konst pool = this.overriddenSymbols.toMutableList()
                konst iterator = pool.listIterator()
                for (symbol in iterator) {
                    if (symbol.owner.overriddenSymbols.isEmpty()) {
                        overriddenSymbols += symbol
                        iterator.remove()
                    } else {
                        symbol.owner.overriddenSymbols.forEach { iterator.add(it) }
                    }
                }
            }

            if (overriddenSymbols.isEmpty()) overriddenSymbols.add(this.symbol)
            return overriddenSymbols
        }

        private fun IrFunction.isExtensionReceiverPrimitive(): Boolean {
            return this.extensionReceiverParameter?.type?.isPrimitiveType() == false
        }

        private fun IrFunction.isReturnTypePrimitiveAsObject(): Boolean {
            for (symbol in getOriginalOverriddenSymbols()) {
                if (!symbol.owner.returnType.isTypeParameter() && !symbol.owner.returnType.isNullable()) {
                    return false
                }
            }
            return true
        }

        private fun IrFunction.isValueParameterPrimitiveAsObject(index: Int): Boolean {
            for (symbol in getOriginalOverriddenSymbols()) {
                if (!symbol.owner.konstueParameters[index].type.isTypeParameter() && !symbol.owner.konstueParameters[index].type.isNullable()) {
                    return false
                }
            }
            return true
        }
    }
}
