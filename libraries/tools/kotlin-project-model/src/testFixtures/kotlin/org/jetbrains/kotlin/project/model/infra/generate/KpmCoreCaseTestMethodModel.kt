/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.project.model.infra.generate

import com.intellij.openapi.util.io.FileUtil
import org.jetbrains.kotlin.generators.model.MethodModel
import org.jetbrains.kotlin.test.util.KtTestUtil
import java.io.File

class KpmCoreCaseTestMethodModel(
    override konst name: String, // equals to name of corresponding KpmCoreCase
    internal konst pathToTestSourcesRootDir: File,
    internal konst pathToTestCase: File,
) : MethodModel {
    object Kind : MethodModel.Kind()

    override konst dataString: String
        get() {
            konst path = FileUtil.getRelativePath(pathToTestSourcesRootDir, pathToTestCase)!!
            return KtTestUtil.getFilePath(File(path))
        }
    override konst tags: List<String>
        get() = emptyList()

    override konst kind: MethodModel.Kind
        get() = Kind
}
