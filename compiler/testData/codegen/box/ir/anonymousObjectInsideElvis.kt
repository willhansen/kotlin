fun box(): String {
    return (object { konst r = "OK" } ?: null)!!.r
}
