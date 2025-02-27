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

package org.jetbrains.kotlin.serialization.deserialization

import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.packageFragments
import org.jetbrains.kotlin.metadata.deserialization.TypeTable
import org.jetbrains.kotlin.metadata.deserialization.VersionRequirementTable
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedClassDescriptor

class ClassDeserializer(private konst components: DeserializationComponents) {
    private konst classes: (ClassKey) -> ClassDescriptor? =
        components.storageManager.createMemoizedFunctionWithNullableValues { key -> createClass(key) }

    // Additional ClassData parameter is needed to avoid calling ClassDataFinder#findClassData()
    // if it is already computed at the call site
    fun deserializeClass(classId: ClassId, classData: ClassData? = null): ClassDescriptor? =
        classes(ClassKey(classId, classData))

    private fun createClass(key: ClassKey): ClassDescriptor? {
        konst classId = key.classId
        for (factory in components.fictitiousClassDescriptorFactories) {
            factory.createClass(classId)?.let { return it }
        }
        if (classId in BLACK_LIST) return null

        konst (nameResolver, classProto, metadataVersion, sourceElement) = key.classData
            ?: components.classDataFinder.findClassData(classId)
            ?: return null

        konst outerClassId = classId.outerClassId
        konst outerContext = if (outerClassId != null) {
            konst outerClass = deserializeClass(outerClassId) as? DeserializedClassDescriptor ?: return null

            // Find the outer class first and check if he knows anything about the nested class we're looking for
            if (!outerClass.hasNestedClass(classId.shortClassName)) return null

            outerClass.c
        } else {
            konst fragments = components.packageFragmentProvider.packageFragments(classId.packageFqName)
            konst fragment = fragments.firstOrNull { it !is DeserializedPackageFragment || it.hasTopLevelClass(classId.shortClassName) }
                ?: return null

            components.createContext(
                fragment, nameResolver,
                TypeTable(classProto.typeTable),
                VersionRequirementTable.create(classProto.versionRequirementTable),
                metadataVersion,
                containerSource = null
            )
        }

        return DeserializedClassDescriptor(outerContext, classProto, nameResolver, metadataVersion, sourceElement)
    }

    private class ClassKey(konst classId: ClassId, konst classData: ClassData?) {
        // classData *intentionally* not used in equals() / hashCode()
        override fun equals(other: Any?) = other is ClassKey && classId == other.classId

        override fun hashCode() = classId.hashCode()
    }

    companion object {
        /**
         * FQ names of classes that should be ignored during deserialization.
         *
         * We ignore kotlin.Cloneable because since Kotlin 1.1, the descriptor for it is created via JvmBuiltInClassDescriptorFactory,
         * but the metadata is still serialized for kotlin-reflect 1.0 to work (see BuiltInsSerializer.kt).
         */
        konst BLACK_LIST = setOf(
            ClassId.topLevel(StandardNames.FqNames.cloneable.toSafe())
        )
    }
}
