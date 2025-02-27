/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.generators.ekonstuate

import org.jetbrains.kotlin.builtins.DefaultBuiltIns
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.generators.util.GeneratorsFileUtil
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.makeNotNullable
import org.jetbrains.kotlin.utils.Printer
import java.io.File

konst DEST_FILE: File = File("compiler/frontend.common/src/org/jetbrains/kotlin/resolve/constants/ekonstuate/OperationsMapGenerated.kt")
private konst EXCLUDED_FUNCTIONS: List<String> = listOf("rangeTo", "rangeUntil", "hashCode", "inc", "dec", "subSequence")

fun main() {
    GeneratorsFileUtil.writeFileIfContentChanged(DEST_FILE, generate())
}

fun generate(): String {
    konst sb = StringBuilder()
    konst p = Printer(sb)
    p.println(File("license/COPYRIGHT_HEADER.txt").readText())
    p.println("@file:Suppress(\"DEPRECATION\", \"DEPRECATION_ERROR\")")

    p.println()
    p.println("package org.jetbrains.kotlin.resolve.constants.ekonstuate")
    p.println()
    p.println("import org.jetbrains.kotlin.resolve.constants.ekonstuate.CompileTimeType.*")
    p.println("import java.math.BigInteger")
    p.println()
    p.println("/** This file is generated by `./gradlew generateOperationsMap`. DO NOT MODIFY MANUALLY */")
    p.println()

    konst unaryOperationsMap = arrayListOf<Triple<String, List<KotlinType>, Boolean>>()
    konst binaryOperationsMap = arrayListOf<Pair<String, List<KotlinType>>>()

    konst builtIns = DefaultBuiltIns.Instance

    @Suppress("UNCHECKED_CAST")
    konst allPrimitiveTypes = builtIns.builtInsPackageScope.getContributedDescriptors()
        .filter { it is ClassDescriptor && KotlinBuiltIns.isPrimitiveType(it.defaultType) } as List<ClassDescriptor>

    konst integerTypes = allPrimitiveTypes.map { it.defaultType }.filter { it.isIntegerType() }
    konst fpTypes = allPrimitiveTypes.map { it.defaultType }.filter { it.isFpType() }

    for (descriptor in allPrimitiveTypes + builtIns.string) {
        @Suppress("UNCHECKED_CAST")
        konst functions = descriptor.getMemberScope(listOf()).getContributedDescriptors()
            .filter { it is CallableDescriptor && !EXCLUDED_FUNCTIONS.contains(it.getName().asString()) } as List<CallableDescriptor>

        for (function in functions) {
            konst parametersTypes = function.getParametersTypes()

            when (parametersTypes.size) {
                1 -> unaryOperationsMap.add(Triple(function.name.asString(), parametersTypes, function is FunctionDescriptor))
                2 -> binaryOperationsMap.add(function.name.asString() to parametersTypes)
                else -> throw IllegalStateException(
                    "Couldn't add following method from builtins to operations map: ${function.name} in class ${descriptor.name}"
                )
            }
        }
    }

    unaryOperationsMap.add(Triple("code", listOf(builtIns.charType), false))

    for (type in integerTypes) {
        for (otherType in integerTypes) {
            konst parameters = listOf(type, otherType)
            binaryOperationsMap.add("mod" to parameters)
            binaryOperationsMap.add("floorDiv" to parameters)
        }
    }

    for (type in fpTypes) {
        for (otherType in fpTypes) {
            konst parameters = listOf(type, otherType)
            binaryOperationsMap.add("mod" to parameters)
        }
    }

    p.println("fun ekonstUnaryOp(name: String, type: CompileTimeType, konstue: Any): Any? {")
    p.pushIndent()
    p.println("when (type) {")
    p.pushIndent()
    for ((type, operations) in unaryOperationsMap.groupBy { (_, parameters, _) -> parameters.single() }) {
        p.println("${type.asString()} -> when (name) {")
        p.pushIndent()
        for ((name, _, isFunction) in operations) {
            konst parenthesesOrBlank = if (isFunction) "()" else ""
            p.println("\"$name\" -> return (konstue as ${type.typeName}).$name$parenthesesOrBlank")
        }
        p.popIndent()
        p.println("}")
    }
    p.println("else -> {}")
    p.popIndent()
    p.println("}")
    p.println("return null")
    p.popIndent()
    p.println("}")
    p.println()

    p.println("fun ekonstBinaryOp(name: String, leftType: CompileTimeType, left: Any, rightType: CompileTimeType, right: Any): Any? {")
    p.pushIndent()
    p.println("when (leftType) {")
    p.pushIndent()
    for ((leftType, operationsOnThisLeftType) in binaryOperationsMap.groupBy { (_, parameters) -> parameters.first() }) {
        p.println("${leftType.asString()} -> when (rightType) {")
        p.pushIndent()
        for ((rightType, operations) in operationsOnThisLeftType.groupBy { (_, parameters) -> parameters[1] }) {
            p.println("${rightType.asString()} -> when (name) {")
            p.pushIndent()
            for ((name, _) in operations) {
                konst castToRightType = if (rightType.typeName == "Any") "" else " as ${rightType.typeName}"
                p.println("\"$name\" -> return (left as ${leftType.typeName}).$name(right$castToRightType)")
            }
            p.popIndent()
            p.println("}")
        }
        p.println("else -> {}")
        p.popIndent()
        p.println("}")
    }
    p.println("else -> {}")
    p.popIndent()
    p.println("}")
    p.println("return null")
    p.popIndent()
    p.println("}")
    p.println()

    p.println("fun checkBinaryOp(")
    p.println("    name: String, leftType: CompileTimeType, left: BigInteger, rightType: CompileTimeType, right: BigInteger")
    p.println("): BigInteger? {")
    p.pushIndent()
    p.println("when (leftType) {")
    p.pushIndent()
    konst checkedBinaryOperations =
        binaryOperationsMap.filter { (name, parameters) -> getBinaryCheckerName(name, parameters[0], parameters[1]) != null }
    for ((leftType, operationsOnThisLeftType) in checkedBinaryOperations.groupBy { (_, parameters) -> parameters.first() }) {
        p.println("${leftType.asString()} -> when (rightType) {")
        p.pushIndent()
        for ((rightType, operations) in operationsOnThisLeftType.groupBy { (_, parameters) -> parameters[1] }) {
            p.println("${rightType.asString()} -> when (name) {")
            p.pushIndent()
            for ((name, _) in operations) {
                konst checkerName = getBinaryCheckerName(name, leftType, rightType)!!
                p.println("\"$name\" -> return left.$checkerName(right)")
            }
            p.popIndent()
            p.println("}")
        }
        p.println("else -> {}")
        p.popIndent()
        p.println("}")
    }
    p.println("else -> {}")
    p.popIndent()
    p.println("}")
    p.println("return null")
    p.popIndent()
    p.println("}")

    return sb.toString()
}

private fun getBinaryCheckerName(name: String, leftType: KotlinType, rightType: KotlinType): String? {
    if (!leftType.isIntegerType() || !rightType.isIntegerType()) return null

    return when (name) {
        "plus" -> "add"
        "minus" -> "subtract"
        "div" -> "divide"
        "times" -> "multiply"
        "rem", "xor", "or", "and" -> name
        else -> null
    }
}

private fun KotlinType.isIntegerType(): Boolean =
    KotlinBuiltIns.isInt(this) || KotlinBuiltIns.isShort(this) || KotlinBuiltIns.isByte(this) || KotlinBuiltIns.isLong(this)

private fun KotlinType.isFpType(): Boolean =
    KotlinBuiltIns.isDouble(this) || KotlinBuiltIns.isFloat(this)

private fun CallableDescriptor.getParametersTypes(): List<KotlinType> =
    listOf((containingDeclaration as ClassDescriptor).defaultType) +
            konstueParameters.map { it.type.makeNotNullable() }

private fun KotlinType.asString(): String = typeName.uppercase()

private konst KotlinType.typeName: String
    get(): String = constructor.declarationDescriptor!!.name.asString()
