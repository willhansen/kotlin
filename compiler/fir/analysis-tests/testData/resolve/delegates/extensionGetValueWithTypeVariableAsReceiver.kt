inline fun <L> runLogged(action: () -> L): L {
    return action()
}

operator fun String.getValue(receiver: Any?, p: Any): String =
    runLogged { this }

konst testK by runLogged { "K" }
