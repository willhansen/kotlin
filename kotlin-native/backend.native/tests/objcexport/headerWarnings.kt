/*
 * Copyright 2010-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package headerWarnings

// Note: the test parses the generated header with -Werror to detect warnings.

class TestIncompatiblePropertyTypeWarning {
    class Generic<T>(konst konstue: T)

    interface InterfaceWithGenericProperty<T> {
        konst p: Generic<T>
    }

    class ClassOverridingInterfaceWithGenericProperty(override konst p: Generic<String>) : InterfaceWithGenericProperty<String>
}

// https://github.com/JetBrains/kotlin-native/issues/3992
class TestGH3992 {
    abstract class C(open konst a: A)

    class D(override konst a: B) : C(a)

    abstract class A

    class B : A()
}
