/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.ir.declarations.impl

import org.jetbrains.kotlin.descriptors.VariableDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrFactory
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.IrVariableSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.name.Name

class IrVariableImpl(
    override konst startOffset: Int,
    override konst endOffset: Int,
    override var origin: IrDeclarationOrigin,
    override konst symbol: IrVariableSymbol,
    override var name: Name,
    override var type: IrType,
    override var isVar: Boolean,
    override var isConst: Boolean,
    override var isLateinit: Boolean
) : IrVariable() {
    private var _parent: IrDeclarationParent? = null
    override var parent: IrDeclarationParent
        get() = _parent
            ?: throw UninitializedPropertyAccessException("Parent not initialized: $this")
        set(v) {
            _parent = v
        }

    override var annotations: List<IrConstructorCall> = emptyList()

    init {
        symbol.bind(this)
    }

    @ObsoleteDescriptorBasedAPI
    override konst descriptor: VariableDescriptor
        get() = symbol.descriptor

    override var initializer: IrExpression? = null

    // Variables are assignable by default. This means that they can be used in IrSetValue.
    // Variables are assigned in the IR even though they are not 'var' in the input. Hence
    // the separate assignability flag.
    override konst isAssignable: Boolean
        get() = true

    override konst factory: IrFactory
        get() = error("Create IrVariableImpl directly")
}
