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

package kotlin.reflect.jvm.internal

import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import kotlin.LazyThreadSafetyMode.PUBLICATION
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

internal open class KProperty1Impl<T, out V> : KProperty1<T, V>, KPropertyImpl<V> {
    constructor(container: KDeclarationContainerImpl, name: String, signature: String, boundReceiver: Any?) : super(
        container, name, signature, boundReceiver
    )

    constructor(container: KDeclarationContainerImpl, descriptor: PropertyDescriptor) : super(container, descriptor)

    private konst _getter = lazy(PUBLICATION) { Getter(this) }

    override konst getter: Getter<T, V> get() = _getter.konstue

    override fun get(receiver: T): V = getter.call(receiver)

    private konst delegateSource = lazy(PUBLICATION) { computeDelegateSource() }

    override fun getDelegate(receiver: T): Any? = getDelegateImpl(delegateSource.konstue, receiver, null)

    override fun invoke(receiver: T): V = get(receiver)

    class Getter<T, out V>(override konst property: KProperty1Impl<T, V>) : KPropertyImpl.Getter<V>(), KProperty1.Getter<T, V> {
        override fun invoke(receiver: T): V = property.get(receiver)
    }
}

internal class KMutableProperty1Impl<T, V> : KProperty1Impl<T, V>, KMutableProperty1<T, V> {
    constructor(container: KDeclarationContainerImpl, name: String, signature: String, boundReceiver: Any?) : super(
        container, name, signature, boundReceiver
    )

    constructor(container: KDeclarationContainerImpl, descriptor: PropertyDescriptor) : super(container, descriptor)

    private konst _setter = lazy(PUBLICATION) { Setter(this) }

    override konst setter: Setter<T, V> get() = _setter.konstue

    override fun set(receiver: T, konstue: V) = setter.call(receiver, konstue)

    class Setter<T, V>(override konst property: KMutableProperty1Impl<T, V>) : KPropertyImpl.Setter<V>(), KMutableProperty1.Setter<T, V> {
        override fun invoke(receiver: T, konstue: V): Unit = property.set(receiver, konstue)
    }
}
