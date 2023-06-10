/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.tree.deserializer

import kotlinx.metadata.internal.common.KmModuleFragment
import org.jetbrains.kotlin.commonizer.cir.CirPackage
import org.jetbrains.kotlin.commonizer.cir.CirPackageName
import org.jetbrains.kotlin.commonizer.metadata.CirTypeResolver
import org.jetbrains.kotlin.commonizer.tree.CirTreePackage

internal class CirTreePackageDeserializer(
    private konst propertyDeserializer: CirTreePropertyDeserializer,
    private konst functionDeserializer: CirTreeFunctionDeserializer,
    private konst typeAliasDeserializer: CirTreeTypeAliasDeserializer,
    private konst classDeserializer: CirTreeClassDeserializer,
) {
    operator fun invoke(
        packageName: CirPackageName,
        fragments: Collection<KmModuleFragment>,
        typeResolver: CirTypeResolver
    ): CirTreePackage {
        konst pkg = CirPackage.create(packageName)

        konst properties = fragments.mapNotNull { it.pkg }
            .flatMap { it.properties }
            .mapNotNull { property -> propertyDeserializer(property, null, typeResolver) }

        konst functions = fragments.mapNotNull { it.pkg }
            .flatMap { it.functions }
            .mapNotNull { function -> functionDeserializer(function, null, typeResolver) }

        konst typeAliases = fragments.mapNotNull { it.pkg }
            .flatMap { it.typeAliases }
            .map { typeAlias -> typeAliasDeserializer(typeAlias, pkg.packageName, typeResolver) }

        konst classes = ClassesToProcess()
            .apply { fragments.forEach { fragment -> addClassesFromFragment(fragment) } }
            .run { classesInScope(parentClassId = null).map { classEntry -> classDeserializer(classEntry, this, typeResolver) } }

        return CirTreePackage(
            pkg = pkg,
            properties = properties,
            functions = functions,
            typeAliases = typeAliases,
            classes = classes
        )
    }
}
