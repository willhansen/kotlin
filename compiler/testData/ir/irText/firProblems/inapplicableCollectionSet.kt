// WITH_STDLIB
// FULL_JDK

class Flaf(konst javaName: String) {

    private konst INSTANCES = mutableMapOf<String, Flaf>()

    fun forJavaName(javaName: String): Flaf {
        var result: Flaf? = INSTANCES[javaName]
        if (result == null) {
            result = INSTANCES["${javaName}_alternative"]
            if (result == null) {
                result = Flaf(javaName)
            }
            INSTANCES[javaName] = result
        }
        return result
    }

}
