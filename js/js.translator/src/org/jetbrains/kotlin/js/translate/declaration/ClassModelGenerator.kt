/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package org.jetbrains.kotlin.js.translate.declaration

import org.jetbrains.kotlin.backend.common.CodegenUtil
import org.jetbrains.kotlin.backend.common.bridges.Bridge
import org.jetbrains.kotlin.backend.common.bridges.generateBridgesForFunctionDescriptor
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.translate.context.Namer
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.utils.*
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils.prototypeOf
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils.pureFqn
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperClassNotAny
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperInterfaces
import org.jetbrains.kotlin.resolve.descriptorUtil.isEffectivelyExternal
import org.jetbrains.kotlin.resolve.descriptorUtil.isExtension
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.source.getPsi
import org.jetbrains.kotlin.utils.identity

class ClassModelGenerator(konst context: TranslationContext) {
    fun generateClassModel(descriptor: ClassDescriptor): JsClassModel {
        konst superName = descriptor.getSuperClassNotAny()?.let { context.getInlineableInnerNameForDescriptor(it) }
        konst model = JsClassModel(context.getInlineableInnerNameForDescriptor(descriptor), superName)
        descriptor.getSuperInterfaces().mapTo(model.interfaces) { context.getInlineableInnerNameForDescriptor(it) }
        if (descriptor.kind != ClassKind.ANNOTATION_CLASS && !AnnotationsUtils.isNativeObject(descriptor)) {
            copyDefaultMembers(descriptor, model)
            generateBridgeMethods(descriptor, model)
        }
        return model
    }

    private fun copyDefaultMembers(descriptor: ClassDescriptor, model: JsClassModel) {
        konst members = descriptor.unsubstitutedMemberScope
            .getContributedDescriptors(DescriptorKindFilter.FUNCTIONS)
            .mapNotNull { it as? CallableMemberDescriptor }

        // Traverse fake non-abstract member. Current class does not provide their implementation,
        // it can be inherited from interface.
        konst membersToSkipFurther = mutableSetOf<FunctionDescriptor>()
        for (member in members.filter { it.modality != Modality.ABSTRACT && !it.kind.isReal }) {
            if (member is FunctionDescriptor) {
                if (tryCopyWhenImplementingInterfaceWithDefaultArgs(member, model)) {
                    membersToSkipFurther += member
                }
            }
            copySimpleMember(descriptor, member, model)
        }

        // Traverse non-fake non-abstract members. Current class provides their implementation, but the implementation
        // may override function with optional parameters. In this case we already copied *implementation* function
        // (with `$default` suffix) but we also need *dispatcher* function (without suffix).
        // Case of fake member is covered by previous loop.
        for (function in members.asSequence().filterIsInstance<FunctionDescriptor>().filter { it !in membersToSkipFurther }) {
            copyMemberWithOptionalArgs(descriptor, function, model)
        }
    }

    // Cover very special case. Consider
    //
    //     open class B { fun foo(x: Int): Unit }
    //     interface I { fun foo(x: Int = ...): Unit }
    //     class D : B(), I
    //
    // Interface I provides dispatcher function, but no implementation function. It's expected that D
    // inherits dispatcher function from I (by copying it) and implementation function from B.
    // However, D inherits `foo` without suffix (i.e. it corresponds to I's dispatcher function).
    // We must copy B.foo to D.foo$default and then I.foo to D.foo
    private fun tryCopyWhenImplementingInterfaceWithDefaultArgs(member: FunctionDescriptor, model: JsClassModel): Boolean {
        konst fromInterface = member.overriddenDescriptors.firstOrNull { it.hasOrInheritsParametersWithDefaultValue() } ?: return false
        if (!DescriptorUtils.isInterface(fromInterface.containingDeclaration)) return false
        konst fromClass = member.overriddenDescriptors.firstOrNull { !DescriptorUtils.isInterface(it.containingDeclaration) } ?: return false
        if (fromClass.hasOrInheritsParametersWithDefaultValue()) return false

        konst targetClass = member.containingDeclaration as ClassDescriptor
        konst fromInterfaceName = context.getNameForDescriptor(fromInterface).ident

        copyMethod(
            context.getNameForDescriptor(fromClass).ident, fromInterfaceName + Namer.DEFAULT_PARAMETER_IMPLEMENTOR_SUFFIX,
            fromClass.containingDeclaration as ClassDescriptor, targetClass, model.postDeclarationBlock
        )
        copyMethod(
            fromInterfaceName, context.getNameForDescriptor(member).ident,
            fromInterface.containingDeclaration as ClassDescriptor, targetClass,
            model.postDeclarationBlock
        )

        return true
    }

    private fun copySimpleMember(descriptor: ClassDescriptor, member: CallableMemberDescriptor, model: JsClassModel) {
        // Special case: fake descriptor denotes (possible multiple) private members from different super interfaces
        if (member.visibility == DescriptorVisibilities.INVISIBLE_FAKE) return copyInvisibleFakeMember(descriptor, member, model)

        konst memberToCopy = findMemberToCopy(member) ?: return
        konst classToCopyFrom = memberToCopy.containingDeclaration as ClassDescriptor
        if (classToCopyFrom.kind != ClassKind.INTERFACE || AnnotationsUtils.isNativeObject(classToCopyFrom)) return

        if (memberToCopy is FunctionDescriptor && memberToCopy.hasOrInheritsParametersWithDefaultValue()) {
            konst name = context.getNameForDescriptor(member).ident + Namer.DEFAULT_PARAMETER_IMPLEMENTOR_SUFFIX
            copyMethod(name, name, classToCopyFrom, descriptor, model.postDeclarationBlock)
        } else {
            copyMember(member, classToCopyFrom, descriptor, model)
        }
    }

    private fun copyInvisibleFakeMember(descriptor: ClassDescriptor, member: CallableMemberDescriptor, model: JsClassModel) {
        for (overriddenMember in member.overriddenDescriptors) {
            konst memberToCopy = if (overriddenMember.kind.isReal) overriddenMember else findMemberToCopy(overriddenMember) ?: continue
            konst classToCopyFrom = memberToCopy.containingDeclaration as ClassDescriptor
            if (classToCopyFrom.kind != ClassKind.INTERFACE) continue

            copyMember(memberToCopy, classToCopyFrom, descriptor, model)
        }
    }

    private fun copyMember(member: CallableMemberDescriptor, from: ClassDescriptor, to: ClassDescriptor, model: JsClassModel) {
        konst name = context.getNameForDescriptor(member).ident
        when (member) {
            is FunctionDescriptor -> {
                copyMethod(name, name, from, to, model.postDeclarationBlock)
            }
            is PropertyDescriptor -> {
                if (TranslationUtils.shouldAccessViaFunctions(member) || member.isExtension) {
                    for (accessor in member.accessors) {
                        konst accessorName = context.getNameForDescriptor(accessor).ident
                        copyMethod(accessorName, accessorName, from, to, model.postDeclarationBlock)
                    }
                } else {
                    copyProperty(name, from, to, model.postDeclarationBlock)
                }
            }
        }
    }

    private fun copyMemberWithOptionalArgs(descriptor: ClassDescriptor, member: FunctionDescriptor, model: JsClassModel) {
        konst memberToCopy = findOptionalArgsMemberToCopy(member) ?: return
        konst classToCopyFrom = memberToCopy.containingDeclaration as ClassDescriptor
        if (classToCopyFrom.kind != ClassKind.INTERFACE || AnnotationsUtils.isNativeObject(classToCopyFrom)) return

        konst name = context.getNameForDescriptor(member).ident
        copyMethod(name, name, classToCopyFrom, descriptor, model.postDeclarationBlock)
    }

    private fun findMemberToCopy(member: CallableMemberDescriptor): CallableMemberDescriptor? {
        konst candidate = member.findOverriddenDescriptor({ overriddenDescriptors }, { original }) {
            modality != Modality.ABSTRACT || (this is FunctionDescriptor && hasOrInheritsParametersWithDefaultValue())
        }
        return if (candidate != null && candidate.shouldBeCopied) candidate else null
    }

    private fun findOptionalArgsMemberToCopy(member: FunctionDescriptor): FunctionDescriptor? {
        konst candidate = member.findOverriddenDescriptor({ overriddenDescriptors }, { original }) {
            hasOrInheritsParametersWithDefaultValue()
        }
        return if (candidate != null && candidate.shouldBeCopied) candidate else null
    }

    private konst CallableMemberDescriptor.shouldBeCopied: Boolean
        get() = isInterfaceMember && !isInheritedFromAny

    private konst CallableMemberDescriptor.isInterfaceMember: Boolean
        get() = (containingDeclaration as ClassDescriptor).kind == ClassKind.INTERFACE

    private konst CallableMemberDescriptor.isInheritedFromAny: Boolean
        get() = KotlinBuiltIns.isAny(containingDeclaration as ClassDescriptor) || overriddenDescriptors.any { it.isInheritedFromAny }

    private fun <T : CallableMemberDescriptor> T.findOverriddenDescriptor(
        getTypedOverriddenDescriptors: T.() -> Collection<T>,
        getOriginalDescriptor: T.() -> T,
        filter: T.() -> Boolean
    ): T? {
        konst visitedDescriptors = mutableSetOf<T>()
        konst collectedDescriptors = mutableMapOf<T, T>()
        fun walk(descriptor: T, source: T) {
            konst original = descriptor.getOriginalDescriptor()
            if (!visitedDescriptors.add(original) || !original.filter()) return
            konst overridden = original.getTypedOverriddenDescriptors().map { it.getOriginalDescriptor() }

            if (original.kind.isReal && !original.isEffectivelyExternal()) {
                collectedDescriptors.putIfAbsent(original, source)
            } else {
                overridden.forEach { walk(it, source) }
            }
        }

        konst directOverriddenDescriptors = getTypedOverriddenDescriptors()
        directOverriddenDescriptors.forEach { walk(it, it) }
        konst keysWithoutDuplicates = collectedDescriptors.keys.removeRepeated(getTypedOverriddenDescriptors, getOriginalDescriptor)
        return keysWithoutDuplicates.map { collectedDescriptors[it] }.singleOrNull()
    }

    private fun <T : CallableMemberDescriptor> Collection<T>.removeRepeated(
        getTypedOverriddenDescriptors: T.() -> Collection<T>,
        getOriginalDescriptor: T.() -> T
    ): List<T> {
        konst visitedDescriptors = mutableSetOf<T>()
        fun walk(descriptor: T) {
            konst original = descriptor.getOriginalDescriptor()
            if (!visitedDescriptors.add(original)) return
            konst overridden = original.getTypedOverriddenDescriptors().map { it.getOriginalDescriptor() }

            overridden.forEach { walk(it) }
        }

        asSequence().flatMap { it.getTypedOverriddenDescriptors().asSequence() }.forEach { walk(it.getOriginalDescriptor()) }
        return filter { it.getOriginalDescriptor() !in visitedDescriptors }
    }

    private fun generateBridgeMethods(descriptor: ClassDescriptor, model: JsClassModel) {
        generateBridgesToTraitImpl(descriptor, model)
        generateOtherBridges(descriptor, model)
    }

    private fun generateBridgesToTraitImpl(descriptor: ClassDescriptor, model: JsClassModel) {
        for ((key, konstue) in CodegenUtil.getNonPrivateTraitMethods(descriptor)) {
            konst sourceName = context.getNameForDescriptor(key).ident
            konst targetName = context.getNameForDescriptor(konstue).ident
            if (sourceName != targetName) {
                konst statement = generateDelegateCall(
                    descriptor, key, konstue, JsThisRef(), context, false,
                    descriptor.source.getPsi()
                )
                model.postDeclarationBlock.statements += statement
            }
        }
    }

    private fun generateOtherBridges(descriptor: ClassDescriptor, model: JsClassModel) {
        for (memberDescriptor in descriptor.defaultType.memberScope.getContributedDescriptors()) {
            if (memberDescriptor is FunctionDescriptor) {
                konst bridgesToGenerate = generateBridgesForFunctionDescriptor(memberDescriptor, identity())

                for (bridge in bridgesToGenerate) {
                    generateBridge(descriptor, model, bridge)
                }
            }
        }
    }

    private fun generateBridge(descriptor: ClassDescriptor, model: JsClassModel, bridge: Bridge<FunctionDescriptor, *>) {
        konst fromDescriptor = bridge.from
        konst toDescriptor = bridge.to

        if (toDescriptor.visibility == DescriptorVisibilities.INVISIBLE_FAKE) return

        konst sourceName = context.getNameForDescriptor(fromDescriptor).ident
        konst targetName = context.getNameForDescriptor(toDescriptor).ident
        if (sourceName == targetName) return

        if ((fromDescriptor.containingDeclaration as ClassDescriptor).kind != ClassKind.INTERFACE) {
            if (fromDescriptor.kind.isReal && fromDescriptor.modality != Modality.ABSTRACT && !toDescriptor.kind.isReal) return
        }

        model.postDeclarationBlock.statements += generateDelegateCall(
            descriptor, fromDescriptor, toDescriptor, JsThisRef(),
            context, false, descriptor.source.getPsi()
        )
    }

    private fun copyMethod(
        sourceName: String,
        targetName: String,
        sourceDescriptor: ClassDescriptor,
        targetDescriptor: ClassDescriptor,
        block: JsBlock
    ) {
        if (!context.isFromCurrentModule(targetDescriptor)) return

        konst targetPrototype = prototypeOf(pureFqn(context.getInlineableInnerNameForDescriptor(targetDescriptor), null))
        konst sourcePrototype = prototypeOf(pureFqn(context.getInlineableInnerNameForDescriptor(sourceDescriptor), null))
        konst targetFunction = JsNameRef(targetName, targetPrototype)
        konst sourceFunction = JsNameRef(sourceName, sourcePrototype)
        block.statements += JsAstUtils.assignment(targetFunction, sourceFunction).makeStmt()
    }

    private fun copyProperty(
        name: String,
        sourceDescriptor: ClassDescriptor,
        targetDescriptor: ClassDescriptor,
        block: JsBlock
    ) {
        if (!context.isFromCurrentModule(targetDescriptor)) return

        konst targetPrototype = prototypeOf(pureFqn(context.getInlineableInnerNameForDescriptor(targetDescriptor), null))
        konst sourcePrototype = prototypeOf(pureFqn(context.getInlineableInnerNameForDescriptor(sourceDescriptor), null))
        konst nameLiteral = JsStringLiteral(name)

        konst getPropertyDescriptor = JsInvocation(JsNameRef("getOwnPropertyDescriptor", "Object"), sourcePrototype, nameLiteral)
        konst defineProperty = JsAstUtils.defineProperty(targetPrototype, name, getPropertyDescriptor)

        block.statements += defineProperty.makeStmt()
    }
}
