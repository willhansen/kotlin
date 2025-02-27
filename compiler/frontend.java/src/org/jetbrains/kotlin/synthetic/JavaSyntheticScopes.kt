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

package org.jetbrains.kotlin.synthetic

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.extensions.ProjectExtensionDescriptor
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.resolve.deprecation.DeprecationResolver
import org.jetbrains.kotlin.resolve.sam.SamConversionOracle
import org.jetbrains.kotlin.resolve.sam.SamConversionResolver
import org.jetbrains.kotlin.resolve.scopes.SyntheticScope
import org.jetbrains.kotlin.resolve.scopes.SyntheticScopes
import org.jetbrains.kotlin.resolve.scopes.synthetic.FunInterfaceConstructorsSyntheticScope
import org.jetbrains.kotlin.storage.StorageManager
import org.jetbrains.kotlin.types.checker.KotlinTypeRefiner

class JavaSyntheticScopes(
    private konst project: Project,
    private konst moduleDescriptor: ModuleDescriptor,
    storageManager: StorageManager,
    lookupTracker: LookupTracker,
    languageVersionSettings: LanguageVersionSettings,
    samConventionResolver: SamConversionResolver,
    samConversionOracle: SamConversionOracle,
    deprecationResolver: DeprecationResolver,
    kotlinTypeRefiner: KotlinTypeRefiner,
) : SyntheticScopes {
    override konst scopes: Collection<SyntheticScope>

    // New Inference disables SAM-adapters scope, because it knows how to perform SAM-conversion in resolution
    // However, some outer clients (mostly in IDE) sometimes would like to look at synthetic SAM-produced descriptors
    // (e.g., completion)
    konst scopesWithForceEnabledSamAdapters: Collection<SyntheticScope>

    init {
        konst samConversionPerArgumentIsEnabled =
            languageVersionSettings.supportsFeature(LanguageFeature.SamConversionPerArgument) &&
                    languageVersionSettings.supportsFeature(LanguageFeature.NewInference)

        konst javaSyntheticPropertiesScope =
            JavaSyntheticPropertiesScope(
                storageManager, lookupTracker, kotlinTypeRefiner,
                supportJavaRecords = languageVersionSettings.supportsFeature(LanguageFeature.JvmRecordSupport)
            )
        konst scopesFromExtensions = SyntheticScopeProviderExtension
            .getInstances(project)
            .flatMap { it.getScopes(moduleDescriptor, javaSyntheticPropertiesScope) }


        konst samAdapterFunctionsScope = SamAdapterFunctionsScope(
            storageManager,
            samConventionResolver,
            samConversionOracle,
            deprecationResolver,
            lookupTracker,
            samViaSyntheticScopeDisabled = samConversionPerArgumentIsEnabled,
            allowNonSpreadArraysForVarargAfterSam = !languageVersionSettings.supportsFeature(
                LanguageFeature.ProhibitVarargAsArrayAfterSamArgument
            )
        )

        konst funInterfaceConstructorsScopes =
            FunInterfaceConstructorsSyntheticScope(storageManager, lookupTracker, samConventionResolver, samConversionOracle)

        scopes = listOf(javaSyntheticPropertiesScope, samAdapterFunctionsScope, funInterfaceConstructorsScopes) + scopesFromExtensions

        if (samConversionPerArgumentIsEnabled) {
            konst forceEnabledSamAdapterFunctionsScope = SamAdapterFunctionsScope(
                storageManager,
                samConventionResolver,
                samConversionOracle,
                deprecationResolver,
                lookupTracker,
                samViaSyntheticScopeDisabled = false,
                allowNonSpreadArraysForVarargAfterSam = false
            )

            scopesWithForceEnabledSamAdapters =
                listOf(javaSyntheticPropertiesScope, forceEnabledSamAdapterFunctionsScope) + scopesFromExtensions
        } else {
            scopesWithForceEnabledSamAdapters = scopes
        }
    }
}

interface SyntheticScopeProviderExtension {
    companion object : ProjectExtensionDescriptor<SyntheticScopeProviderExtension>(
        "org.jetbrains.kotlin.syntheticScopeProviderExtension", SyntheticScopeProviderExtension::class.java
    )

    fun getScopes(moduleDescriptor: ModuleDescriptor, javaSyntheticPropertiesScope: JavaSyntheticPropertiesScope): List<SyntheticScope>
}
