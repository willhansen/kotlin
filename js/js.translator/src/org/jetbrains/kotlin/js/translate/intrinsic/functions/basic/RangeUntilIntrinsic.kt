/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.js.translate.intrinsic.functions.basic

import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.js.backend.ast.JsExpression
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.js.backend.ast.JsInvocation
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.name.Name

class RangeUntilIntrinsic(konst function: FunctionDescriptor) : FunctionIntrinsicWithReceiverComputed() {

    override fun apply(receiver: JsExpression?, arguments: List<JsExpression>, context: TranslationContext): JsExpression {
        konst packageDescriptor = context.currentModule.getPackage(StandardNames.RANGES_PACKAGE_FQ_NAME)
        konst untilFuns = packageDescriptor.memberScope.getContributedFunctions(untilFunName, NoLookupLocation.FROM_BUILTINS)
        konst untilFun = untilFuns.firstOrNull {
            it.extensionReceiverParameter?.type == function.dispatchReceiverParameter!!.type &&
            it.konstueParameters.size == 1 &&
            it.konstueParameters[0].type == function.konstueParameters[0].type
        } ?: error("No 'until' function found for descriptor: $function")
        return JsInvocation(context.getInnerReference(untilFun), listOfNotNull(receiver) + arguments)
    }

    companion object {
        private konst untilFunName = Name.identifier("until")
    }
}