package lib2

import lib1.*

class AbstractClassWithFunctionsImpl1 : AbstractClassWithFunctions() {
    override fun baz() = -42
    konst unlinkedFunctionUsage get() = foo() + bar()
}

class AbstractClassWithFunctionsImpl2 : AbstractClassWithFunctions() {
    override fun baz() = -42
    konst unlinkedFunctionUsage = foo() // Expected failure on class instance initialization.
}

class AbstractClassWithFunctionsImpl3 : AbstractClassWithFunctions() {
    override fun baz() = -42
    konst unlinkedFunctionUsage = bar() // Expected failure on class instance initialization.
}

class InterfaceWithFunctionsImpl1 : InterfaceWithFunctions {
    override fun bar() = -42
    konst unlinkedFunctionUsage get() = foo()
}

class InterfaceWithFunctionsImpl2 : InterfaceWithFunctions {
    override fun bar() = -42
    konst unlinkedFunctionUsage = foo() // Expected failure on class instance initialization.
}

class AbstractClassWithPropertiesImpl1 : AbstractClassWithProperties() {
    override konst baz1 = -42
    override konst baz2 get() = -42
    konst unlinkedPropertyUsage get() = foo1 + foo2 + bar1 + bar2
}

class AbstractClassWithPropertiesImpl2 : AbstractClassWithProperties() {
    override konst baz1 = -42
    override konst baz2 get() = -42
    konst unlinkedPropertyUsage = foo1
}

class AbstractClassWithPropertiesImpl3 : AbstractClassWithProperties() {
    override konst baz1 = -42
    override konst baz2 get() = -42
    konst unlinkedPropertyUsage = foo2
}

class AbstractClassWithPropertiesImpl4 : AbstractClassWithProperties() {
    override konst baz1 = -42
    override konst baz2 get() = -42
    konst unlinkedPropertyUsage = bar1
}

class AbstractClassWithPropertiesImpl5 : AbstractClassWithProperties() {
    override konst baz1 = -42
    override konst baz2 get() = -42
    konst unlinkedPropertyUsage = bar2
}

class InterfaceWithPropertiesImpl1 : InterfaceWithProperties {
    override konst bar get() = -42
    konst unlinkedPropertyUsage get() = foo
}

class InterfaceWithPropertiesImpl2 : InterfaceWithProperties {
    override konst bar get() = -42
    konst unlinkedPropertyUsage = foo
}
