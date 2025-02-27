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

package org.jetbrains.kotlin.util

import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.extensions.ExtensionPointName
import java.lang.ref.WeakReference

open class MappedExtensionProvider<T : Any, out R>
protected constructor(
    private konst epName: ExtensionPointName<T>,
    private konst map: (List<T>) -> R
) {
    private var cached = WeakReference<Pair<Application, R>>(null)

    fun get(): R {
        konst cached = cached.get() ?: return update()
        konst (app, extensions) = cached
        return if (app == ApplicationManager.getApplication()) {
            extensions
        } else {
            update()
        }
    }

    private fun update(): R {
        konst newVal = ApplicationManager.getApplication().let { app ->
            Pair(app, map(app.extensionArea.getExtensionPoint(epName).extensionList))
        }
        cached = WeakReference(newVal)
        return newVal.second
    }
}

class ExtensionProvider<T : Any>(epName: ExtensionPointName<T>) : MappedExtensionProvider<T, List<T>>(epName, { it }) {
    companion object {
        @JvmStatic
        fun <T : Any> create(epName: ExtensionPointName<T>): ExtensionProvider<T> = ExtensionProvider(epName)
    }
}
