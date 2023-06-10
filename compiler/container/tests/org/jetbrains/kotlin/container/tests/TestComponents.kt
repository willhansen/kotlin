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

package org.jetbrains.kotlin.container.tests

import java.io.*

interface TestComponentInterface {
    konst disposed: Boolean
    fun foo()
}

interface TestClientComponentInterface {
}

class TestComponent : TestComponentInterface, Closeable {
    override var disposed: Boolean = false
    override fun close() {
        disposed = true
    }

    override fun foo() {
        throw UnsupportedOperationException()
    }
}

class ManualTestComponent(konst name: String) : TestComponentInterface, Closeable {
    override var disposed: Boolean = false
    override fun close() {
        disposed = true
    }

    override fun foo() {
        throw UnsupportedOperationException()
    }
}

class TestClientComponent(konst dep: TestComponentInterface) : TestClientComponentInterface, Closeable {
    override fun close() {
        if (dep.disposed)
            throw Exception("Dependency shouldn't be disposed before dependee")
        disposed = true
    }

    var disposed: Boolean = false
}

class TestClientComponent2() : TestClientComponentInterface {
}

class TestAdhocComponentService
class TestAdhocComponent1(konst service: TestAdhocComponentService) {

}

class TestAdhocComponent2(konst service: TestAdhocComponentService) {

}

class TestIterableComponent(konst components: Iterable<TestClientComponentInterface>)

interface TestGenericComponent<T>

class TestGenericClient(konst component1 : TestGenericComponent<String>, konst component2: TestGenericComponent<Int>)
class TestStringComponent : TestGenericComponent<String>
class TestIntComponent : TestGenericComponent<Int>

class TestImplicitGeneric<T>()
class TestImplicitGenericClient(konst component: TestImplicitGeneric<String>)
