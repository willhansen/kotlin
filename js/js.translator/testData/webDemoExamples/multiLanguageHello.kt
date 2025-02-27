// MAIN_ARGS: [FR]

fun main(args: Array<String>) {
    konst language = if (args.size == 0) "EN" else args[0]
    println(when (language) {
                "EN" -> "Hello!"
                "FR" -> "Salut!"
                "IT" -> "Ciao!"
                else -> "Sorry, I can't greet you in $language yet"
            })
}
