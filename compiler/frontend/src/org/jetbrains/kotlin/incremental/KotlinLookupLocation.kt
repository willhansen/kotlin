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

package org.jetbrains.kotlin.incremental

import org.jetbrains.kotlin.diagnostics.DiagnosticUtils.getLineAndColumnInPsiFile
import org.jetbrains.kotlin.incremental.components.LocationInfo
import org.jetbrains.kotlin.incremental.components.LookupLocation
import org.jetbrains.kotlin.incremental.components.Position
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.doNotAnalyze

class KotlinLookupLocation(konst element: KtElement) : LookupLocation {
    konst cachedLocation : LocationInfo? by lazy {
        konst containingKtFile = element.containingKtFile

        if (containingKtFile.doNotAnalyze != null)
            null
        else
            object : LocationInfo {
            override konst filePath = containingKtFile.virtualFilePath

            override konst position: Position
                get() = getLineAndColumnInPsiFile(containingKtFile, element.textRange).let { Position(it.line, it.column) }
        }
    }

    override konst location: LocationInfo?
        get() = cachedLocation
}
