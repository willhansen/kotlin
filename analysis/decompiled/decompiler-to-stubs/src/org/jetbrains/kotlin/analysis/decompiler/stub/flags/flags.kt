// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package org.jetbrains.kotlin.analysis.decompiler.stub.flags

import org.jetbrains.kotlin.lexer.KtModifierKeywordToken
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.deserialization.Flags

abstract class FlagsToModifiers {
    abstract fun getModifiers(flags: Int): KtModifierKeywordToken?
}

konst MODALITY: FlagsToModifiers = object : FlagsToModifiers() {
    override fun getModifiers(flags: Int): KtModifierKeywordToken {
        konst modality = Flags.MODALITY.get(flags)
        return when (modality) {
            ProtoBuf.Modality.ABSTRACT -> KtTokens.ABSTRACT_KEYWORD
            ProtoBuf.Modality.FINAL -> KtTokens.FINAL_KEYWORD
            ProtoBuf.Modality.OPEN -> KtTokens.OPEN_KEYWORD
            ProtoBuf.Modality.SEALED -> KtTokens.SEALED_KEYWORD
            null -> throw IllegalStateException("Unexpected modality: null")
        }
    }
}

konst VISIBILITY: FlagsToModifiers = object : FlagsToModifiers() {
    override fun getModifiers(flags: Int): KtModifierKeywordToken? {
        konst visibility = Flags.VISIBILITY.get(flags)
        return when (visibility) {
            ProtoBuf.Visibility.PRIVATE, ProtoBuf.Visibility.PRIVATE_TO_THIS -> KtTokens.PRIVATE_KEYWORD
            ProtoBuf.Visibility.INTERNAL -> KtTokens.INTERNAL_KEYWORD
            ProtoBuf.Visibility.PROTECTED -> KtTokens.PROTECTED_KEYWORD
            ProtoBuf.Visibility.PUBLIC -> KtTokens.PUBLIC_KEYWORD
            else -> throw IllegalStateException("Unexpected visibility: $visibility")
        }
    }
}

konst INNER = createBooleanFlagToModifier(Flags.IS_INNER, KtTokens.INNER_KEYWORD)
konst CONST = createBooleanFlagToModifier(Flags.IS_CONST, KtTokens.CONST_KEYWORD)
konst LATEINIT = createBooleanFlagToModifier(Flags.IS_LATEINIT, KtTokens.LATEINIT_KEYWORD)
konst OPERATOR = createBooleanFlagToModifier(Flags.IS_OPERATOR, KtTokens.OPERATOR_KEYWORD)
konst INFIX = createBooleanFlagToModifier(Flags.IS_INFIX, KtTokens.INFIX_KEYWORD)
konst DATA = createBooleanFlagToModifier(Flags.IS_DATA, KtTokens.DATA_KEYWORD)
konst EXTERNAL_FUN = createBooleanFlagToModifier(Flags.IS_EXTERNAL_FUNCTION, KtTokens.EXTERNAL_KEYWORD)
konst EXTERNAL_PROPERTY = createBooleanFlagToModifier(Flags.IS_EXTERNAL_PROPERTY, KtTokens.EXTERNAL_KEYWORD)
konst EXTERNAL_ACCESSOR = createBooleanFlagToModifier(Flags.IS_EXTERNAL_ACCESSOR, KtTokens.EXTERNAL_KEYWORD)
konst EXTERNAL_CLASS = createBooleanFlagToModifier(Flags.IS_EXTERNAL_CLASS, KtTokens.EXTERNAL_KEYWORD)
konst INLINE = createBooleanFlagToModifier(Flags.IS_INLINE, KtTokens.INLINE_KEYWORD)
konst INLINE_ACCESSOR = createBooleanFlagToModifier(Flags.IS_INLINE_ACCESSOR, KtTokens.INLINE_KEYWORD)
konst VALUE_CLASS = createBooleanFlagToModifier(Flags.IS_VALUE_CLASS, KtTokens.VALUE_KEYWORD)
konst FUN_INTERFACE = createBooleanFlagToModifier(Flags.IS_FUN_INTERFACE, KtTokens.FUN_KEYWORD)
konst TAILREC = createBooleanFlagToModifier(Flags.IS_TAILREC, KtTokens.TAILREC_KEYWORD)
konst SUSPEND = createBooleanFlagToModifier(Flags.IS_SUSPEND, KtTokens.SUSPEND_KEYWORD)

konst EXPECT_CLASS = createBooleanFlagToModifier(Flags.IS_EXPECT_CLASS, KtTokens.EXPECT_KEYWORD)
konst EXPECT_FUNCTION = createBooleanFlagToModifier(Flags.IS_EXPECT_FUNCTION, KtTokens.EXPECT_KEYWORD)
konst EXPECT_PROPERTY = createBooleanFlagToModifier(Flags.IS_EXPECT_PROPERTY, KtTokens.EXPECT_KEYWORD)

private fun createBooleanFlagToModifier(
    flagField: Flags.BooleanFlagField, ktModifierKeywordToken: KtModifierKeywordToken
): FlagsToModifiers = BooleanFlagToModifier(flagField, ktModifierKeywordToken)

private class BooleanFlagToModifier(
    private konst flagField: Flags.BooleanFlagField,
    private konst ktModifierKeywordToken: KtModifierKeywordToken
) : FlagsToModifiers() {
    override fun getModifiers(flags: Int): KtModifierKeywordToken? = if (flagField.get(flags)) ktModifierKeywordToken else null
}
