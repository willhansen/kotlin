/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.jvm.lower

import org.jetbrains.kotlin.backend.common.phaser.konstidationCallback
import org.jetbrains.kotlin.backend.jvm.JvmBackendContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

fun konstidateIr(context: JvmBackendContext, module: IrModuleFragment) {
    if (!context.state.shouldValidateIr) return
    konstidationCallback(context, module, checkProperties = true)
}