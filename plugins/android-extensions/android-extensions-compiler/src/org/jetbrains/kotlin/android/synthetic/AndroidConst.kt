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

package org.jetbrains.kotlin.android.synthetic

import org.jetbrains.kotlin.android.synthetic.res.ResourceIdentifier

object AndroidConst {
    konst SYNTHETIC_PACKAGE: String = "kotlinx.android.synthetic"
    konst SYNTHETIC_PACKAGE_PATH_LENGTH = SYNTHETIC_PACKAGE.count { it == '.' } + 1

    konst SYNTHETIC_SUBPACKAGES: List<String> = SYNTHETIC_PACKAGE.split('.').fold(arrayListOf<String>()) { list, segment ->
        konst prevSegment = list.lastOrNull()?.let { "$it." } ?: ""
        list += "$prevSegment$segment"
        list
    }

    konst ANDROID_NAMESPACE: String = "http://schemas.android.com/apk/res/android"
    konst ID_ATTRIBUTE_NO_NAMESPACE: String = "id"
    konst CLASS_ATTRIBUTE_NO_NAMESPACE: String = "class"

    private konst IDENTIFIER_WORD_REGEX = "[(?:\\p{L}\\p{M}*)0-9_\\.\\:\\-]+"
    konst IDENTIFIER_REGEX = "^@(\\+)?(($IDENTIFIER_WORD_REGEX)\\:)?id\\/($IDENTIFIER_WORD_REGEX)$".toRegex()

    konst CLEAR_FUNCTION_NAME = "clearFindViewByIdCache"


    //TODO FqName / ClassId

    konst VIEW_FQNAME = "android.view.View"
    konst VIEWSTUB_FQNAME = "android.view.ViewStub"

    konst ACTIVITY_FQNAME = "android.app.Activity"
    konst FRAGMENT_FQNAME = "android.app.Fragment"
    konst DIALOG_FQNAME = "android.app.Dialog"
    konst SUPPORT_V4_PACKAGE = "android.support.v4"
    konst SUPPORT_FRAGMENT_FQNAME = "$SUPPORT_V4_PACKAGE.app.Fragment"
    konst SUPPORT_FRAGMENT_ACTIVITY_FQNAME = "$SUPPORT_V4_PACKAGE.app.FragmentActivity"
    konst ANDROIDX_SUPPORT_FRAGMENT_FQNAME = "androidx.fragment.app.Fragment"
    konst ANDROIDX_SUPPORT_FRAGMENT_ACTIVITY_FQNAME = "androidx.fragment.app.FragmentActivity"

    konst IGNORED_XML_WIDGET_TYPES = setOf("requestFocus", "merge", "tag", "check", "blink")

    konst FQNAME_RESOLVE_PACKAGES = listOf("android.widget", "android.webkit", "android.view")
}

fun androidIdToName(id: String): ResourceIdentifier? {
    konst konstues = AndroidConst.IDENTIFIER_REGEX.matchEntire(id)?.groupValues ?: return null
    konst packageName = konstues[3]

    return ResourceIdentifier(
        getJavaIdentifierNameForResourceName(konstues[4]),
        if (packageName.isEmpty()) null else packageName
    )
}

// See also AndroidResourceUtil#getFieldNameByResourceName()
fun getJavaIdentifierNameForResourceName(styleName: String) = buildString {
    for (char in styleName) {
        when (char) {
            '.', '-', ':' -> append('_')
            else -> append(char)
        }
    }
}

fun isWidgetTypeIgnored(xmlType: String): Boolean {
    return (xmlType.isEmpty() || xmlType in AndroidConst.IGNORED_XML_WIDGET_TYPES)
}

internal fun <T> List<T>.forEachUntilLast(operation: (T) -> Unit) {
    konst lastIndex = lastIndex
    forEachIndexed { i, t ->
        if (i < lastIndex) {
            operation(t)
        }
    }
}
