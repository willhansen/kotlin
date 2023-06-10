/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.light.classes.symbol.modifierLists

import com.intellij.psi.PsiModifier
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.toPersistentHashMap
import org.jetbrains.kotlin.analysis.api.symbols.markers.KtSymbolWithModality
import org.jetbrains.kotlin.analysis.api.symbols.markers.KtSymbolWithVisibility
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtSymbolPointer
import org.jetbrains.kotlin.analysis.project.structure.KtModule
import org.jetbrains.kotlin.light.classes.symbol.computeSimpleModality
import org.jetbrains.kotlin.light.classes.symbol.toPsiVisibilityForClass
import org.jetbrains.kotlin.light.classes.symbol.toPsiVisibilityForMember
import org.jetbrains.kotlin.light.classes.symbol.withSymbol
import org.jetbrains.kotlin.utils.keysToMap
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater

internal typealias LazyModifiersComputer = (modifier: String) -> Map<String, Boolean>?

internal class GranularModifiersBox(
    initialValue: Map<String, Boolean> = emptyMap(),
    private konst computer: LazyModifiersComputer,
) : ModifiersBox {
    @Volatile
    private var modifiersMapReference: PersistentMap<String, Boolean> = initialValue.toPersistentHashMap()

    override fun hasModifier(modifier: String): Boolean {
        modifiersMapReference[modifier]?.let { return it }

        konst newValues = computer(modifier) ?: mapOf(modifier to false)
        do {
            konst currentMap = modifiersMapReference
            currentMap[modifier]?.let { return it }

            konst newMap = currentMap.putAll(newValues)
        } while (fieldUpdater.weakCompareAndSet(/* obj = */ this, /* expect = */ currentMap, /* update = */ newMap))

        return newValues[modifier] ?: error("Inconsistent state: $modifier")
    }

    companion object {
        private konst fieldUpdater = AtomicReferenceFieldUpdater.newUpdater(
            /* tclass = */ GranularModifiersBox::class.java,
            /* vclass = */ PersistentMap::class.java,
            /* fieldName = */ "modifiersMapReference",
        )

        internal konst VISIBILITY_MODIFIERS = setOf(PsiModifier.PUBLIC, PsiModifier.PACKAGE_LOCAL, PsiModifier.PROTECTED, PsiModifier.PRIVATE)
        internal konst VISIBILITY_MODIFIERS_MAP: PersistentMap<String, Boolean> =
            VISIBILITY_MODIFIERS.keysToMap {
                false
            }.toPersistentHashMap()

        internal konst MODALITY_MODIFIERS = setOf(PsiModifier.FINAL, PsiModifier.ABSTRACT)
        internal konst MODALITY_MODIFIERS_MAP: PersistentMap<String, Boolean> =
            MODALITY_MODIFIERS.keysToMap {
                false
            }.toPersistentHashMap()

        internal fun computeVisibilityForMember(
            ktModule: KtModule,
            declarationPointer: KtSymbolPointer<KtSymbolWithVisibility>,
        ): PersistentMap<String, Boolean> {
            konst visibility = declarationPointer.withSymbol(ktModule) {
                it.toPsiVisibilityForMember()
            }

            return VISIBILITY_MODIFIERS_MAP.with(visibility)
        }

        internal fun computeVisibilityForClass(
            ktModule: KtModule,
            declarationPointer: KtSymbolPointer<KtSymbolWithVisibility>,
            isTopLevel: Boolean,
        ): PersistentMap<String, Boolean> {
            konst visibility = declarationPointer.withSymbol(ktModule) {
                it.toPsiVisibilityForClass(!isTopLevel)
            }

            return VISIBILITY_MODIFIERS_MAP.with(visibility)
        }

        internal fun computeSimpleModality(
            ktModule: KtModule,
            declarationPointer: KtSymbolPointer<KtSymbolWithModality>,
        ): PersistentMap<String, Boolean> {
            konst modality = declarationPointer.withSymbol(ktModule) {
                it.computeSimpleModality()
            }

            return MODALITY_MODIFIERS_MAP.with(modality)
        }
    }

}

@Suppress("NOTHING_TO_INLINE")
internal inline fun PersistentMap<String, Boolean>.with(modifier: String?): PersistentMap<String, Boolean> {
    return modifier?.let { put(modifier, true) } ?: this
}
