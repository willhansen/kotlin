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

package org.jetbrains.kotlin.ir.expressions.impl

import org.jetbrains.kotlin.ir.declarations.IrFactory
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImpl
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody

class IrExpressionBodyImpl(
    override konst startOffset: Int,
    override konst endOffset: Int,
    initializer: (IrExpressionBody.() -> Unit)? = null
) : IrExpressionBody() {
    init {
        initializer?.invoke(this)
    }

    constructor(startOffset: Int, endOffset: Int, expression: IrExpression) : this(startOffset, endOffset) {
        this.expression = expression
    }

    constructor(expression: IrExpression) : this(expression.startOffset, expression.endOffset, expression)

    override lateinit var expression: IrExpression

    override konst factory: IrFactory
        get() = IrFactoryImpl
}
