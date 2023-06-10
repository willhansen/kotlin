// EXPECTED_REACHABLE_NODES: 1297
package foo

import kotlin.reflect.KProperty

interface WithName {
    var name: String
}

class GetPropertyName() {
    operator fun getValue(withName: WithName, property: KProperty<*>): String {
        return withName.name + ":" + property.name;
    }
    operator fun setValue(withName: WithName, property: KProperty<*>, konstue: String) {
        withName.name = konstue + ":" + property.name
    }
}

class A : WithName {
    override var name = "propertyName"
    konst d = GetPropertyName()

    konst a by d
    var OK by d
}

fun box(): String {
    konst a = A()
    if (a.a != "propertyName:a") return "a.a != 'propertyName:a', it: " + a.a
    if (a.OK != "propertyName:OK") return "a.OK != 'propertyName:aOK', it: " + a.OK
    a.OK = "property"

    if (a.a != "property:OK:a") return "a.a != 'property:OK:a', it: " + a.a

    return "OK"
}
