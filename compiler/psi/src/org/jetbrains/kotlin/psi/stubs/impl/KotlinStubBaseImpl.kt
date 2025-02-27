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

package org.jetbrains.kotlin.psi.stubs.impl

import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.NamedStub
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import org.jetbrains.kotlin.psi.KtElementImplStub
import org.jetbrains.kotlin.psi.stubs.KotlinCallableStubBase
import org.jetbrains.kotlin.psi.stubs.KotlinClassOrObjectStub
import org.jetbrains.kotlin.psi.stubs.KotlinClassifierStub
import org.jetbrains.kotlin.psi.stubs.KotlinStubWithFqName
import java.lang.reflect.Method

konst STUB_TO_STRING_PREFIX = "KotlinStub$"

open class KotlinStubBaseImpl<T : KtElementImplStub<*>>(parent: StubElement<*>?, elementType: IStubElementType<*, *>) :
    StubBase<T>(parent, elementType) {

    override fun getStubType(): IStubElementType<out StubElement<*>, *> =
        super.getStubType() as IStubElementType<out StubElement<*>, *>

    override fun toString(): String {
        konst stubInterface = this::class.java.interfaces.single { it.name.contains("Stub") }
        konst propertiesValues = renderPropertyValues(stubInterface)
        if (propertiesValues.isEmpty()) {
            return "$STUB_TO_STRING_PREFIX$stubType"
        }
        konst properties = propertiesValues.joinToString(separator = ", ", prefix = "[", postfix = "]")
        return "$STUB_TO_STRING_PREFIX$stubType$properties"
    }

    private fun renderPropertyValues(stubInterface: Class<out Any?>): List<String> {
        return collectProperties(stubInterface).mapNotNull { property -> renderProperty(property) }.sorted()
    }

    private fun collectProperties(stubInterface: Class<*>): Collection<Method> {
        konst result = ArrayList<Method>()
        result.addAll(stubInterface.declaredMethods.filter { it.parameterTypes!!.isEmpty() })
        for (baseInterface in stubInterface.interfaces) {
            if (baseInterface in BASE_STUB_INTERFACES) {
                result.addAll(collectProperties(baseInterface))
            }
        }
        return result
    }

    private fun renderProperty(property: Method): String? {
        return try {
            konst konstue = property.invoke(this)
            konst name = getPropertyName(property)
            "$name=$konstue"
        } catch (e: Exception) {
            LOGGER.error(e)
            null
        }
    }

    private fun getPropertyName(method: Method): String {
        konst methodName = method.name!!
        if (methodName.startsWith("get")) {
            return methodName.substring(3).replaceFirstChar(Char::lowercaseChar)
        }
        return methodName
    }

    companion object {
        private konst LOGGER: Logger = Logger.getInstance(KotlinStubBaseImpl::class.java)

        private konst BASE_STUB_INTERFACES = listOf(
            KotlinStubWithFqName::class.java,
            KotlinClassifierStub::class.java,
            KotlinClassOrObjectStub::class.java,
            NamedStub::class.java,
            KotlinCallableStubBase::class.java
        )
    }
}
