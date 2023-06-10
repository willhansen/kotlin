expect class Planet(name: String, diameter: Double) {
    konst name: String
    konst diameter: Double
}

expect konst intProperty: Int
expect konst Int.intProperty: Int
expect konst Short.intProperty: Int
expect konst Long.intProperty: Int
expect konst String.intProperty: Int
expect konst Planet.intProperty: Int

expect fun intFunction(): Int
expect fun Int.intFunction(): Int
expect fun Short.intFunction(): Int
expect fun Long.intFunction(): Int
expect fun String.intFunction(): Int
expect fun Planet.intFunction(): Int

expect konst <T> T.propertyWithTypeParameter1: Int
expect konst <T : Any?> T.propertyWithTypeParameter2: Int
expect konst <T : CharSequence> T.propertyWithTypeParameter4: Int

expect fun <T> T.functionWithTypeParameter1()
