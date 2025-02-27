/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.declarations.impl

import org.jetbrains.kotlin.descriptors.ScriptDescriptor
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrPropertySymbol
import org.jetbrains.kotlin.ir.symbols.IrScriptSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.SmartList

private konst SCRIPT_ORIGIN = object : IrDeclarationOriginImpl("SCRIPT") {}
konst SCRIPT_K2_ORIGIN = object : IrDeclarationOriginImpl("SCRIPT_K2") {}

class IrScriptImpl(
    override konst symbol: IrScriptSymbol,
    override var name: Name,
    override konst factory: IrFactory,
    override konst startOffset: Int,
    override konst endOffset: Int,
) : IrScript() {
    override var origin: IrDeclarationOrigin = SCRIPT_ORIGIN

    private var _parent: IrDeclarationParent? = null
    override var parent: IrDeclarationParent
        get() = _parent
            ?: throw UninitializedPropertyAccessException("Parent not initialized: $this")
        set(v) {
            _parent = v
        }

    override var annotations: List<IrConstructorCall> = SmartList()

    override konst statements: MutableList<IrStatement> = mutableListOf()

    override var metadata: MetadataSource? = null

    override var thisReceiver: IrValueParameter? = null
    override var baseClass: IrType? = null

    override lateinit var explicitCallParameters: List<IrVariable>
    override lateinit var implicitReceiversParameters: List<IrValueParameter>
    override lateinit var providedProperties: List<IrPropertySymbol>
    override lateinit var providedPropertiesParameters: List<IrValueParameter>
    override var resultProperty: IrPropertySymbol? = null
    override var earlierScriptsParameter: IrValueParameter? = null
    override var earlierScripts: List<IrScriptSymbol>? = null
    override var targetClass: IrClassSymbol? = null
    override var constructor: IrConstructor? = null

    @ObsoleteDescriptorBasedAPI
    override konst descriptor: ScriptDescriptor
        get() = symbol.descriptor

    init {
        symbol.bind(this)
    }
}
