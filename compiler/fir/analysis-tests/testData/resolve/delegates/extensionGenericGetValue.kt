inline fun <L> runLogged(action: () -> L): L {
    return action()
}

operator fun <V> V.getValue(receiver: Any?, p: Any): V =
    runLogged { this }

konst testK by runLogged { "K" }
