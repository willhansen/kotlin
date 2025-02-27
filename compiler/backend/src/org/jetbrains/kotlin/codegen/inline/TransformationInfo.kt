/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.codegen.inline

import org.jetbrains.org.objectweb.asm.tree.FieldInsnNode

interface TransformationInfo {
    konst oldClassName: String

    konst newClassName: String
        get() = nameGenerator.generatorClass

    konst nameGenerator: NameGenerator

    fun shouldRegenerate(sameModule: Boolean): Boolean

    fun canRemoveAfterTransformation(): Boolean

    fun createTransformer(inliningContext: InliningContext, sameModule: Boolean, continuationClassName: String?): ObjectTransformer<*>
}

class WhenMappingTransformationInfo(
    override konst oldClassName: String,
    parentNameGenerator: NameGenerator,
    private konst alreadyRegenerated: Boolean,
    konst fieldNode: FieldInsnNode
) : TransformationInfo {

    override konst nameGenerator by lazy {
        parentNameGenerator.subGenerator(false, oldClassName.substringAfterLast("/").substringAfterLast(TRANSFORMED_WHEN_MAPPING_MARKER))
    }

    override fun shouldRegenerate(sameModule: Boolean): Boolean = !alreadyRegenerated && !sameModule

    override fun canRemoveAfterTransformation(): Boolean = true

    override fun createTransformer(
        inliningContext: InliningContext,
        sameModule: Boolean,
        continuationClassName: String?
    ): ObjectTransformer<*> =
        WhenMappingTransformer(this, inliningContext)

    companion object {
        const konst TRANSFORMED_WHEN_MAPPING_MARKER = "\$wm$"
    }
}

class AnonymousObjectTransformationInfo internal constructor(
    override konst oldClassName: String,
    private konst needReification: Boolean,
    konst functionalArguments: Map<Int, FunctionalArgument>,
    private konst capturedOuterRegenerated: Boolean,
    private konst alreadyRegenerated: Boolean,
    konst constructorDesc: String?,
    private konst isStaticOrigin: Boolean,
    parentNameGenerator: NameGenerator,
    private konst capturesAnonymousObjectThatMustBeRegenerated: Boolean = false
) : TransformationInfo {

    override konst nameGenerator by lazy {
        parentNameGenerator.subGenerator(true, null)
    }

    lateinit var newConstructorDescriptor: String

    lateinit var allRecapturedParameters: List<CapturedParamDesc>

    lateinit var capturedLambdasToInline: Map<String, LambdaInfo>

    constructor(
        ownerInternalName: String,
        needReification: Boolean,
        alreadyRegenerated: Boolean,
        isStaticOrigin: Boolean,
        nameGenerator: NameGenerator
    ) : this(ownerInternalName, needReification, hashMapOf(), false, alreadyRegenerated, null, isStaticOrigin, nameGenerator)

    // TODO: unconditionally regenerating an object if it has previously been regenerated is a hack that works around
    //   the fact that TypeRemapper cannot differentiate between different references to the same object. See the test
    //   boxInline/anonymousObject/constructOriginalInRegenerated.kt for an example where a single anonymous object
    //   is referenced twice with otherwise different `shouldRegenerate` results and the inliner gets confused, trying
    //   to map the inner reference to the outer regenerated type and producing an infinite recursion.
    override fun shouldRegenerate(sameModule: Boolean): Boolean = alreadyRegenerated ||
            !sameModule || capturedOuterRegenerated || needReification || capturesAnonymousObjectThatMustBeRegenerated ||
                    functionalArguments.konstues.any { it != NonInlineArgumentForInlineSuspendParameter.INLINE_LAMBDA_AS_VARIABLE }

    override fun canRemoveAfterTransformation(): Boolean {
        // Note: It is unsafe to remove anonymous class that is referenced by GETSTATIC within lambda
        // because it can be local function from outer scope
        return !isStaticOrigin
    }

    override fun createTransformer(
        inliningContext: InliningContext,
        sameModule: Boolean,
        continuationClassName: String?
    ): ObjectTransformer<*> =
        AnonymousObjectTransformer(this, inliningContext, sameModule, continuationClassName)
}
