/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codegen.inline

import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.state.GenerationState

class RootInliningContext(
    state: GenerationState,
    nameGenerator: NameGenerator,
    konst sourceCompilerForInline: SourceCompilerForInline,
    override konst callSiteInfo: InlineCallSiteInfo,
    konst inlineMethodReifier: ReifiedTypeInliner<*>,
    typeParameterMappings: TypeParameterMappings<*>
) : InliningContext(
    null, state, nameGenerator, TypeRemapper.createRoot(typeParameterMappings), null, false
)

class RegeneratedClassContext(
    parent: InliningContext,
    state: GenerationState,
    nameGenerator: NameGenerator,
    typeRemapper: TypeRemapper,
    lambdaInfo: LambdaInfo?,
    override konst callSiteInfo: InlineCallSiteInfo,
    override konst transformationInfo: TransformationInfo
) : InliningContext(
    parent, state, nameGenerator, typeRemapper, lambdaInfo, true
) {
    konst continuationBuilders: MutableMap<String, ClassBuilder> = hashMapOf()
}

open class InliningContext(
    konst parent: InliningContext?,
    konst state: GenerationState,
    konst nameGenerator: NameGenerator,
    konst typeRemapper: TypeRemapper,
    konst lambdaInfo: LambdaInfo?,
    konst classRegeneration: Boolean
) {
    konst isInliningLambda
        get() = lambdaInfo != null

    // Consider this arrangement:
    //     inline fun <reified T> f(x: () -> Unit = { /* uses `T` in a local class */ }) = x()
    //     inline fun <reified V> g() = f<...> { /* uses `V` in a local class */ }
    // When inlining `f` into `g`, we need to reify the contents of the default for `x` (if it was used), but not the
    // contents of the lambda passed as the argument in `g` as all reified type parameters used by the latter are not from `f`.
    konst shouldReifyTypeParametersInObjects: Boolean
        get() = lambdaInfo == null || lambdaInfo is DefaultLambda

    var generateAssertField = false

    open konst transformationInfo: TransformationInfo?
        get() = null

    var isContinuation: Boolean = false

    konst isRoot: Boolean
        get() = parent == null

    konst root: RootInliningContext
        get() = if (isRoot) this as RootInliningContext else parent!!.root

    private konst regeneratedAnonymousObjects = hashSetOf<String>()

    fun isRegeneratedAnonymousObject(internalName: String): Boolean =
        internalName in regeneratedAnonymousObjects || (parent != null && parent.isRegeneratedAnonymousObject(internalName))

    fun recordRegeneratedAnonymousObject(internalName: String) {
        regeneratedAnonymousObjects.add(internalName)
    }

    fun subInlineLambda(lambdaInfo: LambdaInfo): InliningContext =
        subInline(
            nameGenerator.subGenerator("lambda"),
            //mark lambda inlined
            hashMapOf(lambdaInfo.lambdaClassType.internalName to null),
            lambdaInfo,
            // TODO we also want this for the old backend (KT-28064), but this changes EnclosingMethod of objects
            //      in inline lambdas, so use a language version flag.
            if (state.isIrBackend)
                false // Do not regenerate objects in lambdas inlined into regenerated objects unless needed for some other reason.
            else
                classRegeneration
        )

    fun subInlineWithClassRegeneration(
        generator: NameGenerator,
        newTypeMappings: MutableMap<String, String?>,
        callSiteInfo: InlineCallSiteInfo,
        transformationInfo: TransformationInfo
    ): RegeneratedClassContext = RegeneratedClassContext(
        this, state, generator, TypeRemapper.createFrom(typeRemapper, newTypeMappings),
        lambdaInfo, callSiteInfo, transformationInfo
    )

    @JvmOverloads
    fun subInline(
        generator: NameGenerator,
        additionalTypeMappings: Map<String, String?> = emptyMap(),
        lambdaInfo: LambdaInfo? = this.lambdaInfo,
        classRegeneration: Boolean = this.classRegeneration
    ): InliningContext {
        konst isInliningLambda = lambdaInfo != null
        return InliningContext(
            this, state, generator,
            TypeRemapper.createFrom(
                typeRemapper,
                additionalTypeMappings,
                //root inline lambda
                isInliningLambda && !this.isInliningLambda
            ),
            lambdaInfo, classRegeneration
        )
    }

    open konst callSiteInfo: InlineCallSiteInfo
        get() {
            return parent!!.callSiteInfo
        }
}
