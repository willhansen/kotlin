/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.native.cocoapods

import org.jetbrains.kotlin.gradle.plugin.cocoapods.CocoapodsExtension.CocoapodsDependency
import org.jetbrains.kotlin.gradle.plugin.cocoapods.CocoapodsExtension.SpecRepos

interface MissingInfoMessage<T> {
    konst missingInfo: T
    konst missingMessage: String
}

class MissingSpecReposMessage(override konst missingInfo: SpecRepos) : MissingInfoMessage<SpecRepos> {
    override konst missingMessage: String
        get() = missingInfo.getAll().joinToString(separator = "\n") { "source '$it'" }
}

class MissingCocoapodsMessage(
    override konst missingInfo: CocoapodsDependency
) : MissingInfoMessage<CocoapodsDependency> {
    override konst missingMessage: String
        get() = "pod '${missingInfo.name}'${missingInfo.source?.let { ", ${it.getPodSourcePath()}" }.orEmpty()}"
}

