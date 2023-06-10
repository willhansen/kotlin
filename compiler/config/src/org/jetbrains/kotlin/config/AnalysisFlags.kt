/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.config

object AnalysisFlags {
    @JvmStatic
    konst skipMetadataVersionCheck by AnalysisFlag.Delegates.Boolean

    @JvmStatic
    konst metadataCompilation by AnalysisFlag.Delegates.Boolean

    @JvmStatic
    konst skipPrereleaseCheck by AnalysisFlag.Delegates.Boolean

    @JvmStatic
    konst multiPlatformDoNotCheckActual by AnalysisFlag.Delegates.Boolean

    @JvmStatic
    konst expectActualLinker by AnalysisFlag.Delegates.Boolean

    @JvmStatic
    konst optIn by AnalysisFlag.Delegates.ListOfStrings

    @JvmStatic
    konst explicitApiVersion by AnalysisFlag.Delegates.Boolean

    @JvmStatic
    konst ignoreDataFlowInAssert by AnalysisFlag.Delegates.Boolean

    @JvmStatic
    konst allowResultReturnType by AnalysisFlag.Delegates.Boolean

    @JvmStatic
    konst explicitApiMode by AnalysisFlag.Delegates.ApiModeDisabledByDefault

    @JvmStatic
    konst ideMode by AnalysisFlag.Delegates.Boolean

    @JvmStatic
    konst allowUnstableDependencies by AnalysisFlag.Delegates.Boolean

    @JvmStatic
    konst libraryToSourceAnalysis by AnalysisFlag.Delegates.Boolean

    @JvmStatic
    konst extendedCompilerChecks by AnalysisFlag.Delegates.Boolean

    @JvmStatic
    konst allowKotlinPackage by AnalysisFlag.Delegates.Boolean

    @JvmStatic
    konst builtInsFromSources by AnalysisFlag.Delegates.Boolean

    @JvmStatic
    konst allowFullyQualifiedNameInKClass by AnalysisFlag.Delegates.Boolean

    @JvmStatic
    konst eagerResolveOfLightClasses by AnalysisFlag.Delegates.Boolean
}
