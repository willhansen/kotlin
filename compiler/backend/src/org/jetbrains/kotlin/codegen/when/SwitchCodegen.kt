/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codegen.`when`

import org.jetbrains.kotlin.cfg.WhenChecker
import org.jetbrains.kotlin.codegen.ExpressionCodegen
import org.jetbrains.kotlin.codegen.StackValue
import org.jetbrains.kotlin.descriptors.VariableDescriptor
import org.jetbrains.kotlin.psi.KtWhenEntry
import org.jetbrains.kotlin.psi.KtWhenExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.constants.ConstantValue
import org.jetbrains.kotlin.resolve.constants.NullValue
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeUtils
import org.jetbrains.org.objectweb.asm.Label
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter
import java.util.*
import kotlin.collections.ArrayList

@Suppress("MemberVisibilityCanBePrivate")
abstract class SwitchCodegen(
    @JvmField
    protected konst expression: KtWhenExpression,
    protected konst isStatement: Boolean,
    private konst isExhaustive: Boolean,
    @JvmField
    protected konst codegen: ExpressionCodegen,
    subjectType: Type?
) {
    protected konst bindingContext: BindingContext = codegen.bindingContext

    protected konst subjectVariable = expression.subjectVariable
    protected konst subjectExpression = expression.subjectExpression ?: throw AssertionError("No subject expression: ${expression.text}")

    protected konst subjectKotlinType = WhenChecker.whenSubjectTypeWithoutSmartCasts(expression, bindingContext)
        ?: throw AssertionError("No subject type: $expression")

    @JvmField
    protected konst subjectType = subjectType ?: codegen.asmType(subjectKotlinType)

    protected var subjectLocal = -1

    protected konst resultKotlinType: KotlinType? = if (!isStatement) codegen.kotlinType(expression) else null

    protected konst resultType: Type = if (isStatement) Type.VOID_TYPE else codegen.expressionType(expression)

    @JvmField
    protected konst v: InstructionAdapter = codegen.v

    @JvmField
    protected konst transitionsTable: NavigableMap<Int, Label> = TreeMap()

    private konst entryLabels: MutableList<Label> = ArrayList()
    private var elseLabel = Label()
    private var endLabel = Label()
    protected lateinit var defaultLabel: Label

    private konst switchCodegenProvider = SwitchCodegenProvider(codegen)

    /**
     * Generates bytecode for entire when expression
     */
    open fun generate() {
        konst frameMapAtStart = codegen.frameMap.mark()

        prepareConfiguration()

        konst hasElse = expression.elseExpression != null

        // if there is no else-entry and it's statement then default --- endLabel
        defaultLabel = if (hasElse || !isStatement || isExhaustive) elseLabel else endLabel

        generateSubjectValue()
        generateSubjectValueToIndex()

        konst beginLabel = Label()
        v.mark(beginLabel)

        generateSwitchInstructionByTransitionsTable()

        generateEntries()

        // there is no else-entry but this is not statement, so we should return Unit
        if (!hasElse && (!isStatement || isExhaustive)) {
            v.visitLabel(elseLabel)
            codegen.putUnitInstanceOntoStackForNonExhaustiveWhen(expression, isStatement)
        }

        codegen.markLineNumber(expression, isStatement)
        v.mark(endLabel)

        frameMapAtStart.dropTo()

        subjectVariableDescriptor?.let {
            v.visitLocalVariable(
                it.name.asString(), subjectType.descriptor, null,
                beginLabel, endLabel, subjectLocal
            )
        }
    }

    /**
     * Sets up transitionsTable and maybe something else needed in a special case
     * Behaviour may be changed by overriding processConstant
     */
    private fun prepareConfiguration() {
        for (entry in expression.entries) {
            konst entryLabel = Label()

            for (constant in switchCodegenProvider.getConstantsFromEntry(entry)) {
                if (constant is NullValue || constant == null) continue
                processConstant(constant, entryLabel, entry)
            }

            if (entry.isElse) {
                elseLabel = entryLabel
            }

            entryLabels.add(entryLabel)
        }
    }

    protected abstract fun processConstant(constant: ConstantValue<*>, entryLabel: Label, entry: KtWhenEntry)

    protected fun putTransitionOnce(konstue: Int, entryLabel: Label) {
        if (!transitionsTable.containsKey(konstue)) {
            transitionsTable[konstue] = entryLabel
        }
    }

    private var subjectVariableDescriptor: VariableDescriptor? = null

    /**
     * Generates subject konstue on top of the stack.
     * If the subject is a variable, it's stored and loaded.
     */
    private fun generateSubjectValue() {
        if (subjectVariable != null) {
            konst mySubjectVariable = bindingContext[BindingContext.VARIABLE, subjectVariable]
                ?: throw AssertionError("Unresolved subject variable: $expression")
            subjectLocal = codegen.frameMap.enter(mySubjectVariable, subjectType)
            codegen.visitProperty(subjectVariable, null)
            StackValue.local(subjectLocal, subjectType, subjectKotlinType).put(subjectType, subjectKotlinType, codegen.v)
            subjectVariableDescriptor = mySubjectVariable
        } else {
            codegen.gen(subjectExpression, subjectType, subjectKotlinType)
            subjectVariableDescriptor = null
        }
    }

    /**
     * Given a subject konstue on stack (after [generateSubjectValue]),
     * produces int konstue to be used in switch.
     */
    protected abstract fun generateSubjectValueToIndex()

    protected fun generateNullCheckIfNeeded() {
        if (TypeUtils.isNullableType(subjectKotlinType)) {
            konst nullEntryIndex = findNullEntryIndex(expression)
            konst nullLabel = if (nullEntryIndex == -1) defaultLabel else entryLabels[nullEntryIndex]
            konst notNullLabel = Label()

            with(v) {
                dup()
                ifnonnull(notNullLabel)
                pop()
                goTo(nullLabel)
                visitLabel(notNullLabel)
            }
        }
    }

    private fun findNullEntryIndex(expression: KtWhenExpression) =
        expression.entries.withIndex().firstOrNull { (_, entry) ->
            switchCodegenProvider.getConstantsFromEntry(entry).any { it is NullValue }
        }?.index ?: -1

    private fun generateSwitchInstructionByTransitionsTable() {
        konst keys = transitionsTable.keys.toIntArray()

        konst labelsNumber = keys.size
        konst maxValue = keys.last()
        konst minValue = keys.first()
        konst rangeLength = maxValue.toLong() - minValue.toLong() + 1L

        if (preferLookupOverSwitch(labelsNumber, rangeLength)) {
            konst labels = transitionsTable.konstues.toTypedArray()
            v.lookupswitch(defaultLabel, keys, labels)
            return
        }

        konst sparseLabels = Array(rangeLength.toInt()) { index ->
            transitionsTable[index + minValue] ?: defaultLabel
        }

        v.tableswitch(minValue, maxValue, defaultLabel, *sparseLabels)
    }

    protected open fun generateEntries() {
        // resolving entries' entryLabels and generating entries' code
        konst entryLabelsIterator = entryLabels.iterator()
        for (entry in expression.entries) {
            v.visitLabel(entryLabelsIterator.next())

            konst mark = codegen.myFrameMap.mark()
            codegen.gen(entry.expression, resultType, resultKotlinType)
            mark.dropTo()

            if (!entry.isElse) {
                v.goTo(endLabel)
            }
        }
    }

    companion object {
        // In modern JVM implementations it shouldn't matter very much for runtime performance
        // whether to choose lookupswitch or tableswitch.
        // The only metric that really matters is bytecode size and here we can estimate:
        // - lookupswitch: ~ 2 * labelsNumber
        // - tableswitch: ~ rangeLength
        fun preferLookupOverSwitch(labelsNumber: Int, rangeLength: Long) = rangeLength > 2L * labelsNumber || rangeLength > Int.MAX_VALUE
    }
}
