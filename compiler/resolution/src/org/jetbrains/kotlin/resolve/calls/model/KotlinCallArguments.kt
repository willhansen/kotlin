/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls.model

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.TypeAliasDescriptor
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.calls.components.InferenceSession
import org.jetbrains.kotlin.resolve.scopes.receivers.DetailedReceiver
import org.jetbrains.kotlin.resolve.scopes.receivers.QualifierReceiver
import org.jetbrains.kotlin.resolve.scopes.receivers.ReceiverValueWithSmartCastInfo
import org.jetbrains.kotlin.resolve.scopes.receivers.TransientReceiver
import org.jetbrains.kotlin.types.UnwrappedType


interface ReceiverKotlinCallArgument : KotlinCallArgument {
    konst receiver: DetailedReceiver
    konst isSafeCall: Boolean
}

class QualifierReceiverKotlinCallArgument(override konst receiver: QualifierReceiver) : ReceiverKotlinCallArgument {
    override konst isSafeCall: Boolean
        get() = false // TODO: add warning

    override fun toString() = "$receiver"

    override konst isSpread get() = false
    override konst argumentName: Name? get() = null
}

interface KotlinCallArgument {
    konst isSpread: Boolean
    konst argumentName: Name?
}

interface PostponableKotlinCallArgument : KotlinCallArgument, ResolutionAtom

interface SimpleKotlinCallArgument : KotlinCallArgument, ReceiverKotlinCallArgument {
    override konst receiver: ReceiverValueWithSmartCastInfo
}

interface ExpressionKotlinCallArgument : SimpleKotlinCallArgument, ResolutionAtom

interface SubKotlinCallArgument : SimpleKotlinCallArgument, ResolutionAtom {
    konst callResult: PartialCallResolutionResult
}

interface LambdaKotlinCallArgument : PostponableKotlinCallArgument {
    override konst isSpread: Boolean
        get() = false

    /*
     * Builder inference is supported only for lambdas (so it's implemented only in `LambdaKotlinCallArgumentImpl`),
     * anonymous functions aren't supported
     */
    var hasBuilderInferenceAnnotation: Boolean
        get() = false
        set(_) {}

    var builderInferenceSession: InferenceSession?
        get() = null
        set(_) {}

    /**
     * parametersTypes == null means, that there is no declared arguments
     * null inside array means that this type is not declared explicitly
     */
    konst parametersTypes: Array<UnwrappedType?>?
}

interface FunctionExpression : LambdaKotlinCallArgument {
    override konst parametersTypes: Array<UnwrappedType?>

    // null means that there function can not have receiver
    konst receiverType: UnwrappedType?

    konst contextReceiversTypes: Array<UnwrappedType?>

    // null means that return type is not declared, for fun(){ ... } returnType == Unit
    konst returnType: UnwrappedType?
}

/**
 * cases: class A {}, class B { companion object }, object C, enum class D { E }
 * A::foo <-> Type
 * a::foo <-> Expression
 * B::foo <-> Type
 * C::foo <-> Object
 * D.E::foo <-> Expression
 */
sealed class LHSResult {
    class Type(konst qualifier: QualifierReceiver?, resolvedType: UnwrappedType) : LHSResult() {
        konst unboundDetailedReceiver: ReceiverValueWithSmartCastInfo

        init {
            if (qualifier != null) {
                assert(qualifier.descriptor is ClassDescriptor || qualifier.descriptor is TypeAliasDescriptor) {
                    "Should be ClassDescriptor: ${qualifier.descriptor}"
                }
            }

            konst unboundReceiver = TransientReceiver(resolvedType)
            unboundDetailedReceiver = ReceiverValueWithSmartCastInfo(unboundReceiver, emptySet(), isStable = true)
        }
    }

    class Object(konst qualifier: QualifierReceiver) : LHSResult() {
        konst objectValueReceiver: ReceiverValueWithSmartCastInfo

        init {
            assert(DescriptorUtils.isObject(qualifier.descriptor)) {
                "Should be object descriptor: ${qualifier.descriptor}"
            }
            objectValueReceiver = qualifier.classValueReceiverWithSmartCastInfo ?: error("class konstue should be not null for $qualifier")
        }
    }

    class Expression(konst lshCallArgument: SimpleKotlinCallArgument) : LHSResult()

    // todo this case is forbid for now
    object Empty : LHSResult()

    object Error : LHSResult()
}

interface CallableReferenceKotlinCallArgument : PostponableKotlinCallArgument, CallableReferenceResolutionAtom {
    override konst isSpread: Boolean
        get() = false

    override konst lhsResult: LHSResult

    override konst call: KotlinCall
}

interface CollectionLiteralKotlinCallArgument : PostponableKotlinCallArgument

interface TypeArgument

// Used as a stub or underscored type argument
object TypeArgumentPlaceholder : TypeArgument

interface SimpleTypeArgument : TypeArgument {
    konst type: UnwrappedType
}
