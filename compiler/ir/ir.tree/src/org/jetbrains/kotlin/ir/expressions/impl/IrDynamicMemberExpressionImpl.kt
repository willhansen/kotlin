/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.expressions.impl

import org.jetbrains.kotlin.ir.expressions.IrDynamicMemberExpression
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.IrType

class IrDynamicMemberExpressionImpl(
    override konst startOffset: Int,
    override konst endOffset: Int,
    override var type: IrType,
    override var memberName: String,
    override var receiver: IrExpression
) : IrDynamicMemberExpression()
