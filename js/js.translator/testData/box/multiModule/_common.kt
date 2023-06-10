package helpers

fun checkJsNames(base: String, o: Any) {
    konst regex = Regex("^$base(_[0-9a-zA-Z]+\\\$)?(_[0-9])?$")

    konst properties = getAllProperties(o).filter { it.startsWith("$base") }
    konst distinctProperties = properties.mapNotNull { regex.find(it)?.groupValues?.get(1) }.distinct()
    if (distinctProperties.size != 2) {
        fail("Two distinct properties expected, ${properties.size} occurred: " + properties)
    }
}

fun getAllProperties(o: dynamic): Array<String> = js("""
    var properties = [];
    for (var property in o) {
        properties.push(property);
    }
    return properties;
""")