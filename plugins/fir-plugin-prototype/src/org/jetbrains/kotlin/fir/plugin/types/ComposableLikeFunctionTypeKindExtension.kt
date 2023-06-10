/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.plugin.types

import org.jetbrains.kotlin.builtins.functions.FunctionTypeKind
import org.jetbrains.kotlin.config.LanguageVersion
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.FirFunctionTypeKindExtension
import org.jetbrains.kotlin.fir.plugin.fqn
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class ComposableLikeFunctionTypeKindExtension(session: FirSession) : FirFunctionTypeKindExtension(session) {
    override fun FunctionTypeKindRegistrar.registerKinds() {
        registerKind(ComposableFunction, KComposableFunction)
    }
}

private konst COMPOSABLE_PACKAGE_FQN = FqName.topLevel(Name.identifier("some"))
private konst MY_COMPOSABLE_ANNOTATION_CLASS_ID = ClassId.topLevel("MyComposable".fqn())

object ComposableFunction : FunctionTypeKind(
    COMPOSABLE_PACKAGE_FQN,
    "MyComposableFunction",
    MY_COMPOSABLE_ANNOTATION_CLASS_ID,
    isReflectType = false
) {
    override konst prefixForTypeRender: String
        get() = "@MyComposable"

    override konst serializeAsFunctionWithAnnotationUntil: String
        get() = LanguageVersion.KOTLIN_2_1.versionString

    override fun reflectKind(): FunctionTypeKind = KComposableFunction
}

object KComposableFunction : FunctionTypeKind(
    COMPOSABLE_PACKAGE_FQN,
    "KMyComposableFunction",
    MY_COMPOSABLE_ANNOTATION_CLASS_ID,
    isReflectType = true
) {
    override konst serializeAsFunctionWithAnnotationUntil: String
        get() = LanguageVersion.KOTLIN_2_1.versionString

    override fun nonReflectKind(): FunctionTypeKind = ComposableFunction
}
