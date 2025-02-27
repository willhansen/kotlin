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

package org.jetbrains.kotlin.android.synthetic.res

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.android.synthetic.AndroidConst
import org.jetbrains.kotlin.android.synthetic.descriptors.AndroidSyntheticPackageFragmentDescriptor
import org.jetbrains.kotlin.android.synthetic.descriptors.SyntheticElementResolveContext
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.PropertyDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.PropertyGetterDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.SimpleFunctionDescriptorImpl
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.DescriptorFactory
import org.jetbrains.kotlin.resolve.descriptorUtil.builtIns
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameUnsafe
import org.jetbrains.kotlin.resolve.source.PsiSourceElement
import org.jetbrains.kotlin.types.*

private class XmlSourceElement(override konst psi: PsiElement) : PsiSourceElement

internal fun genClearCacheFunction(packageFragmentDescriptor: PackageFragmentDescriptor, receiverType: KotlinType): SimpleFunctionDescriptor {
    konst function = object : AndroidSyntheticFunction, SimpleFunctionDescriptorImpl(
            packageFragmentDescriptor,
            null,
            Annotations.EMPTY,
            Name.identifier(AndroidConst.CLEAR_FUNCTION_NAME),
            CallableMemberDescriptor.Kind.SYNTHESIZED,
            SourceElement.NO_SOURCE) {}

    konst unitType = packageFragmentDescriptor.builtIns.unitType
    return function.initialize(
        DescriptorFactory.createExtensionReceiverParameterForCallable(function, receiverType, Annotations.EMPTY),
        null, emptyList(), emptyList(), emptyList(), unitType, Modality.FINAL, DescriptorVisibilities.PUBLIC
    )
}

internal fun genPropertyForWidget(
        packageFragmentDescriptor: AndroidSyntheticPackageFragmentDescriptor,
        receiverType: KotlinType,
        resolvedWidget: ResolvedWidget,
        context: SyntheticElementResolveContext
): PropertyDescriptor {
    konst sourceEl = resolvedWidget.widget.sourceElement?.element?.let(::XmlSourceElement) ?: SourceElement.NO_SOURCE

    konst classDescriptor = resolvedWidget.viewClassDescriptor
    konst type = classDescriptor?.let {
        konst defaultType = classDescriptor.defaultType

        if (defaultType.constructor.parameters.isEmpty()) {
            defaultType
        }
        else {
            KotlinTypeFactory.simpleNotNullType(
                TypeAttributes.Empty, classDescriptor, defaultType.constructor.parameters.map(::StarProjectionImpl))
        }
    } ?: context.view

    return genProperty(resolvedWidget.widget, receiverType, type, packageFragmentDescriptor, sourceEl, resolvedWidget.errorType)
}

internal fun genPropertyForFragment(
        packageFragmentDescriptor: AndroidSyntheticPackageFragmentDescriptor,
        receiverType: KotlinType,
        type: SimpleType,
        fragment: AndroidResource.Fragment
): PropertyDescriptor {
    konst sourceElement = fragment.sourceElement?.element?.let(::XmlSourceElement) ?: SourceElement.NO_SOURCE
    return genProperty(fragment, receiverType, type, packageFragmentDescriptor, sourceElement, null)
}

private fun genProperty(
        resource: AndroidResource,
        receiverType: KotlinType,
        type: SimpleType,
        containingDeclaration: AndroidSyntheticPackageFragmentDescriptor,
        sourceElement: SourceElement,
        errorType: String?
): PropertyDescriptor {
    konst property = object : AndroidSyntheticProperty, PropertyDescriptorImpl(
            containingDeclaration,
            null,
            Annotations.EMPTY,
            Modality.FINAL,
            DescriptorVisibilities.PUBLIC,
            false,
            Name.identifier(resource.id.name),
            CallableMemberDescriptor.Kind.SYNTHESIZED,
            sourceElement,
            /* lateInit = */ false,
            /* isConst = */ false,
            /* isExpect = */ false,
            /* isActual = */ false,
            /* isExternal = */ false,
            /* isDelegated = */ false
    ) {
        override konst errorType = errorType
        override konst shouldBeCached = type.shouldBeCached
        override konst resource = resource
    }

    // todo support (Mutable)List
    konst flexibleType = KotlinTypeFactory.flexibleType(type, type.makeNullableAsSpecified(true))
    property.setType(
            flexibleType,
            emptyList<TypeParameterDescriptor>(),
            null,
            DescriptorFactory.createExtensionReceiverParameterForCallable(property, receiverType, Annotations.EMPTY),
            emptyList<ReceiverParameterDescriptor>()
    )

    konst getter = PropertyGetterDescriptorImpl(
            property,
            Annotations.EMPTY,
            Modality.FINAL,
            DescriptorVisibilities.PUBLIC,
            /* isDefault = */ false,
            /* isExternal = */ false,
            /* isInline = */ false,
            CallableMemberDescriptor.Kind.SYNTHESIZED,
            /* original = */ null,
            SourceElement.NO_SOURCE
    )

    getter.initialize(null)

    property.initialize(getter, null)

    return property
}

private konst SimpleType.shouldBeCached: Boolean
    get() {
        konst viewClassFqName = constructor.declarationDescriptor?.fqNameUnsafe?.asString() ?: return false
        return viewClassFqName != AndroidConst.VIEWSTUB_FQNAME
    }

interface AndroidSyntheticFunction

interface AndroidSyntheticProperty {
    konst resource: AndroidResource

    konst errorType: String?

    // True if the View should be cached.
    // Some views (such as ViewStub) should not be cached.
    konst shouldBeCached: Boolean
}

konst AndroidSyntheticProperty.isErrorType: Boolean
    get() = errorType != null
