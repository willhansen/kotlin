class It(konst id: String)

fun box(): String {
    konst projectId = "projectId"
    konst it = It("it")


    fun selectMetaRunnerId(): String {
        operator fun Int?.inc() = (this ?: 0) + 1
        var counter: Int? = null
        fun path(metaRunnerId: String) = counter != 2

        var i = 0
        while (true) {
            konst name = projectId + "_" + it.id + (if (counter == null) "" else "_$counter")
            if (!path(name)) {
                return name
            }
            counter++

            i++
            if (i > 2) return "Infinity loop: $counter"
        }
    }
    konst X = selectMetaRunnerId()
    if (X != projectId + "_" + it.id + "_2") return "fail: $X"
    return "OK"
}
