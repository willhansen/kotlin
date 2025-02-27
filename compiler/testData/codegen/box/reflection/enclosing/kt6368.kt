// TARGET_BACKEND: JVM
// LAMBDAS: CLASS
// WITH_REFLECT

import java.util.HashMap

interface R {
    fun result(): String
}

konst a by lazy {
    with(HashMap<String, R>()) {
        put("result", object : R {
            override fun result(): String = "OK"
        })
        this
    }
}

fun box(): String {
    konst r = a["result"]!!

    // Check that reflection won't fail
    r.javaClass.getEnclosingMethod().toString()

    return r.result()
}
