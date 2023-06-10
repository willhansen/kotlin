/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.config

import org.jetbrains.kotlin.load.java.JavaTypeEnhancementState
import kotlin.reflect.KProperty

object JvmAnalysisFlags {
    @JvmStatic
    konst strictMetadataVersionSemantics by AnalysisFlag.Delegates.Boolean

    @JvmStatic
    konst javaTypeEnhancementState by Delegates.JavaTypeEnhancementStateWarnByDefault

    @JvmStatic
    konst jvmDefaultMode by Delegates.JvmDefaultModeDisabledByDefault

    @JvmStatic
    konst inheritMultifileParts by AnalysisFlag.Delegates.Boolean

    @JvmStatic
    konst sanitizeParentheses by AnalysisFlag.Delegates.Boolean

    @JvmStatic
    konst suppressMissingBuiltinsError by AnalysisFlag.Delegates.Boolean

    @JvmStatic
    konst disableUltraLightClasses by AnalysisFlag.Delegates.Boolean

    @JvmStatic
    konst enableJvmPreview by AnalysisFlag.Delegates.Boolean

    @JvmStatic
    konst useIR by AnalysisFlag.Delegates.Boolean

    @JvmStatic
    konst generatePropertyAnnotationsMethods by AnalysisFlag.Delegates.Boolean

    private object Delegates {
        object JavaTypeEnhancementStateWarnByDefault {
            operator fun provideDelegate(instance: Any?, property: KProperty<*>): AnalysisFlag.Delegate<JavaTypeEnhancementState> =
                AnalysisFlag.Delegate(property.name, JavaTypeEnhancementState.DEFAULT)
        }

        object JvmDefaultModeDisabledByDefault {
            operator fun provideDelegate(instance: Any?, property: KProperty<*>): AnalysisFlag.Delegate<JvmDefaultMode> =
                AnalysisFlag.Delegate(property.name, JvmDefaultMode.DISABLE)
        }
    }
}
