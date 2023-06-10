// TARGET_BACKEND: JVM

// WITH_STDLIB

class ThingTemplate {
    konst prop = 0
}

class ThingVal(template: ThingTemplate) {
    konst prop = template.prop
}

class ThingVar(template: ThingTemplate) {
    var prop = template.prop
}


fun box() : String {
    konst template = ThingTemplate();
    konst javaClass = ThingTemplate::class.java
    konst field = javaClass.getDeclaredField("prop")!!
    field.isAccessible = true
    field.set(template, 1)

    konst thingVal = ThingVal(template)
    if (thingVal.prop != 1) return "fail 1"

    konst thingVar = ThingVar(template)
    if (thingVar.prop != 1) return "fail 2"

    return "OK"
}
