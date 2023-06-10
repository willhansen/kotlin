// FIR_IDENTICAL

@OptIn(ExperimentalStdlibApi::class)
@JsExternalInheritorsOnly
external interface ExternalInterfaceX {
    konst x: String
}

@OptIn(ExperimentalStdlibApi::class)
@JsExternalInheritorsOnly
external open class ExternalOpenClassX {
    konst x: String
}

// check interfaces

external interface ExternalInterfaceXY : ExternalInterfaceX {
    konst y: String
}

interface <!JS_EXTERNAL_INHERITORS_ONLY!>InterfaceXY<!> : ExternalInterfaceX {
    konst y: String
}

interface <!JS_EXTERNAL_INHERITORS_ONLY!>InterfaceXYZ<!> : ExternalInterfaceXY {
    konst z: String
}

// check objects

external object ExternalObjectXY : ExternalInterfaceX {
    override konst x: String
    konst y: String
}

external object ExternalObjectXYZ : ExternalInterfaceXY {
    override konst x: String
    override konst y: String
    konst z: String
}

external object ExternalObjectXZ : ExternalOpenClassX {
    konst z: String
}

<!JS_EXTERNAL_INHERITORS_ONLY!>object ObjectXY<!> : ExternalInterfaceX {
    override konst x: String = "X"
    konst y: String = "Y"
}

<!JS_EXTERNAL_INHERITORS_ONLY!>object ObjectXYZ<!> : ExternalInterfaceXY {
    override konst x: String = "X"
    override konst y: String = "Y"
    konst z: String = "Z"
}

<!JS_EXTERNAL_INHERITORS_ONLY!>object ObjectXZ<!> : ExternalOpenClassX() {
    konst z: String = "Z"
}

// check classes

external class ExternalClassXY : ExternalInterfaceX {
    override konst x: String
    konst y: String
}

external class ExternalClassXYZ : ExternalInterfaceXY {
    override konst x: String
    override konst y: String
    konst z: String
}

external open class ExternalOpenClassXZ : ExternalOpenClassX {
    konst z: String
}

class <!JS_EXTERNAL_INHERITORS_ONLY!>ClassXY<!> : ExternalInterfaceX {
    override konst x: String = "X"
    konst y: String = "Y"
}

class <!JS_EXTERNAL_INHERITORS_ONLY!>ClassXYZ<!> : ExternalInterfaceXY {
    override konst x: String = "X"
    override konst y: String = "Y"
    konst z: String = "Z"
}

class <!JS_EXTERNAL_INHERITORS_ONLY!>ClassXZ<!> : ExternalOpenClassX() {
    konst z: String = "Z"
}

class <!JS_EXTERNAL_INHERITORS_ONLY!>ClassXZY<!> : ExternalOpenClassXZ() {
    konst y: String = "Y"
}

// check nested

external class ExternalClassNameSpace {
    @OptIn(ExperimentalStdlibApi::class)
    @JsExternalInheritorsOnly
    interface NestedInterfaceX {
        konst x: String
    }

    interface NestedInterfaceXY : NestedInterfaceX {
        konst y: String
    }
}

external interface ExternalInterfaceXY2 : ExternalClassNameSpace.NestedInterfaceX {
    konst y: String
}

external interface ExternalInterfaceXYZ2 : ExternalClassNameSpace.NestedInterfaceXY {
    konst z: String
}

interface <!JS_EXTERNAL_INHERITORS_ONLY!>InterfaceXY2<!> : ExternalClassNameSpace.NestedInterfaceX {
    konst y: String
}

interface <!JS_EXTERNAL_INHERITORS_ONLY!>InterfaceXYZ2<!> : ExternalClassNameSpace.NestedInterfaceXY {
    konst z: String
}

// multiple inheritance

external class ExternalClassXY2 : ExternalInterfaceX, ExternalOpenClassX {
    konst y: String
}

class <!JS_EXTERNAL_INHERITORS_ONLY, JS_EXTERNAL_INHERITORS_ONLY!>ClassXY2<!> : ExternalInterfaceX, ExternalOpenClassX() {
    konst y: String = "Y"
}
