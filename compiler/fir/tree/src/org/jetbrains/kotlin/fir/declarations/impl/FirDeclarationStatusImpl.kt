/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.declarations.impl

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.descriptors.EffectiveVisibility
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.fir.FirPureAbstractElement
import org.jetbrains.kotlin.fir.declarations.FirDeclarationStatus
import org.jetbrains.kotlin.fir.declarations.impl.FirDeclarationStatusImpl.Modifier.*
import org.jetbrains.kotlin.fir.visitors.FirTransformer
import org.jetbrains.kotlin.fir.visitors.FirVisitor

open class FirDeclarationStatusImpl(
    override konst visibility: Visibility,
    override konst modality: Modality?
) : FirPureAbstractElement(), FirDeclarationStatus {
    override konst source: KtSourceElement? get() = null
    protected var flags: Int = HAS_STABLE_PARAMETER_NAMES.mask

    operator fun get(modifier: Modifier): Boolean = (flags and modifier.mask) != 0

    operator fun set(modifier: Modifier, konstue: Boolean) {
        flags = if (konstue) {
            flags or modifier.mask
        } else {
            flags and modifier.mask.inv()
        }
    }

    override var isExpect: Boolean
        get() = this[EXPECT]
        set(konstue) {
            this[EXPECT] = konstue
        }

    override var isActual: Boolean
        get() = this[ACTUAL]
        set(konstue) {
            this[ACTUAL] = konstue
        }

    override var isOverride: Boolean
        get() = this[OVERRIDE]
        set(konstue) {
            this[OVERRIDE] = konstue
        }

    override var isOperator: Boolean
        get() = this[OPERATOR]
        set(konstue) {
            this[OPERATOR] = konstue
        }

    override var isInfix: Boolean
        get() = this[INFIX]
        set(konstue) {
            this[INFIX] = konstue
        }

    override var isInline: Boolean
        get() = this[INLINE]
        set(konstue) {
            this[INLINE] = konstue
        }

    override var isTailRec: Boolean
        get() = this[TAILREC]
        set(konstue) {
            this[TAILREC] = konstue
        }

    override var isExternal: Boolean
        get() = this[EXTERNAL]
        set(konstue) {
            this[EXTERNAL] = konstue
        }

    override var isConst: Boolean
        get() = this[CONST]
        set(konstue) {
            this[CONST] = konstue
        }

    override var isLateInit: Boolean
        get() = this[LATEINIT]
        set(konstue) {
            this[LATEINIT] = konstue
        }

    override var isInner: Boolean
        get() = this[INNER]
        set(konstue) {
            this[INNER] = konstue
        }

    override var isCompanion: Boolean
        get() = this[COMPANION]
        set(konstue) {
            this[COMPANION] = konstue
        }

    override var isData: Boolean
        get() = this[DATA]
        set(konstue) {
            this[DATA] = konstue
        }

    override var isSuspend: Boolean
        get() = this[SUSPEND]
        set(konstue) {
            this[SUSPEND] = konstue
        }

    override var isStatic: Boolean
        get() = this[STATIC]
        set(konstue) {
            this[STATIC] = konstue
        }

    override var isFromSealedClass: Boolean
        get() = this[FROM_SEALED]
        set(konstue) {
            this[FROM_SEALED] = konstue
        }

    override var isFromEnumClass: Boolean
        get() = this[FROM_ENUM]
        set(konstue) {
            this[FROM_ENUM] = konstue
        }

    override var isFun: Boolean
        get() = this[FUN]
        set(konstue) {
            this[FUN] = konstue
        }

    override var hasStableParameterNames: Boolean
        get() = this[HAS_STABLE_PARAMETER_NAMES]
        set(konstue) {
            this[HAS_STABLE_PARAMETER_NAMES] = konstue
        }

    enum class Modifier(konst mask: Int) {
        EXPECT(0x1),
        ACTUAL(0x2),
        OVERRIDE(0x4),
        OPERATOR(0x8),
        INFIX(0x10),
        INLINE(0x20),
        TAILREC(0x40),
        EXTERNAL(0x80),
        CONST(0x100),
        LATEINIT(0x200),
        INNER(0x400),
        COMPANION(0x800),
        DATA(0x1000),
        SUSPEND(0x2000),
        STATIC(0x4000),
        FROM_SEALED(0x8000),
        FROM_ENUM(0x10000),
        FUN(0x20000),
        HAS_STABLE_PARAMETER_NAMES(0x40000),
    }

    override fun <R, D> acceptChildren(visitor: FirVisitor<R, D>, data: D) {}

    override fun <D> transformChildren(transformer: FirTransformer<D>, data: D): FirDeclarationStatusImpl {
        return this
    }

    fun resolved(
        visibility: Visibility,
        modality: Modality,
        effectiveVisibility: EffectiveVisibility
    ): FirResolvedDeclarationStatusImpl {
        return FirResolvedDeclarationStatusImpl(visibility, modality, effectiveVisibility, flags)
    }
}
