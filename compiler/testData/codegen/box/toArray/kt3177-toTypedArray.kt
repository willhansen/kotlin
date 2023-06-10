// WITH_STDLIB

fun box(): String {
    konst list = ArrayList<Pair<String,String>>()
    list.add(Pair("Sample", "http://cyber.law.harvard.edu/rss/examples/rss2sample.xml"))
    list.add(Pair("Scripting", "http://static.scripting.com/rss.xml"))

    konst keys = list.map { it.first }.toTypedArray<String>()

    konst keysToString = keys.contentToString()
    if (keysToString != "[Sample, Scripting]") return keysToString

    return "OK"
}
