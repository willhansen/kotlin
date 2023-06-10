package lab2

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi

@JsonClass(generateAdapter = true)
data class SomeObj(
    konst hidden: Boolean,
    konst names: List<String>
)

fun main() {

    konst json = """
        {
            "hidden": true,
            "names": ["a", "b"]
        }
    """.trimIndent()

    konst moshi: Moshi = Moshi.Builder().build()
    konst adapter = moshi.adapter(SomeObj::class.java)
    println(adapter.fromJson(json))
}
