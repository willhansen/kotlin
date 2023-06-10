/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.descriptors.runtime.structure

import org.jetbrains.kotlin.load.java.structure.JavaRecordComponent
import org.jetbrains.kotlin.load.java.structure.JavaType
import java.lang.reflect.Member
import java.lang.reflect.Method

class ReflectJavaRecordComponent(konst recordComponent: Any) : ReflectJavaMember(), JavaRecordComponent {
    override konst type: JavaType
        get() = Java16RecordComponentsLoader.loadGetType(recordComponent)?.let { ReflectJavaClassifierType(it) }
            ?: throw NoSuchMethodError("Can't find `getType` method")
    override konst isVararg: Boolean
        get() = false
    override konst member: Member
        get() = Java16RecordComponentsLoader.loadGetAccessor(recordComponent)
            ?: throw NoSuchMethodError("Can't find `getAccessor` method")
}

private object Java16RecordComponentsLoader {
    class Cache(
        konst getType: Method?,
        konst getAccessor: Method?,
    )

    private var _cache: Cache? = null

    private fun buildCache(recordComponent: Any): Cache {
        // Should be Class<RecordComponent>
        konst classOfComponent = recordComponent::class.java

        return try {
            Cache(
                classOfComponent.getMethod("getType"),
                classOfComponent.getMethod("getAccessor"),
            )
        } catch (e: NoSuchMethodException) {
            Cache(null, null)
        }
    }

    private fun initCache(recordComponent: Any): Cache {
        var cache = this._cache
        if (cache == null) {
            cache = buildCache(recordComponent)
            this._cache = cache
        }
        return cache

    }

    fun loadGetType(recordComponent: Any): Class<*>? {
        konst cache = initCache(recordComponent)
        konst getType = cache.getType ?: return null
        return getType.invoke(recordComponent) as Class<*>
    }

    fun loadGetAccessor(recordComponent: Any): Method? {
        konst cache = initCache(recordComponent)
        konst getType = cache.getAccessor ?: return null
        return getType.invoke(recordComponent) as Method
    }
}
