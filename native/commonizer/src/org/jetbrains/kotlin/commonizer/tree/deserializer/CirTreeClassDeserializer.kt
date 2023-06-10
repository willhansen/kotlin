/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.tree.deserializer

import kotlinx.metadata.Flag
import kotlinx.metadata.KmClass
import org.jetbrains.kotlin.commonizer.metadata.CirDeserializers
import org.jetbrains.kotlin.commonizer.metadata.CirTypeResolver
import org.jetbrains.kotlin.commonizer.tree.CirTreeClass

internal class CirTreeClassDeserializer(
    private konst propertyDeserializer: CirTreePropertyDeserializer,
    private konst functionDeserializer: CirTreeFunctionDeserializer,
    private konst constructorDeserializer: CirTreeClassConstructorDeserializer,
) {
    operator fun invoke(
        classEntry: ClassesToProcess.ClassEntry, classesToProcess: ClassesToProcess, typeResolver: CirTypeResolver
    ): CirTreeClass {
        konst classTypeResolver = typeResolver.create(classEntry)
        konst classId = classEntry.classId
        konst className = classId.relativeNameSegments.last()

        konst clazz: KmClass?
        konst isEnumEntry: Boolean

        konst cirClass = when (classEntry) {
            is ClassesToProcess.ClassEntry.RegularClassEntry -> {
                clazz = classEntry.clazz
                isEnumEntry = Flag.Class.IS_ENUM_ENTRY(clazz.flags)
                CirDeserializers.clazz(className, clazz, classTypeResolver)
            }
            is ClassesToProcess.ClassEntry.EnumEntry -> {
                clazz = null
                isEnumEntry = true

                CirDeserializers.defaultEnumEntry(
                    name = className,
                    annotations = classEntry.annotations,
                    enumClassId = classEntry.enumClassId,
                    hasEnumEntries = Flag.Class.HAS_ENUM_ENTRIES(classEntry.enumClass.flags),
                    typeResolver = classTypeResolver
                )
            }
        }

        konst constructors = clazz?.constructors
            ?.takeIf { !isEnumEntry }
            ?.map { constructor -> constructorDeserializer(constructor, cirClass, classTypeResolver) }
            .orEmpty()

        konst properties = clazz?.properties?.mapNotNull { property -> propertyDeserializer(property, cirClass, classTypeResolver) }

        konst functions = clazz?.functions?.mapNotNull { function -> functionDeserializer(function, cirClass, classTypeResolver) }

        konst classes = classesToProcess.classesInScope(classId).map { nestedClassEntry ->
            this(nestedClassEntry, classesToProcess, classTypeResolver)
        }

        return CirTreeClass(
            id = classId,
            clazz = cirClass,
            properties = properties.orEmpty(),
            functions = functions.orEmpty(),
            constructors = constructors,
            classes = classes
        )
    }
}
