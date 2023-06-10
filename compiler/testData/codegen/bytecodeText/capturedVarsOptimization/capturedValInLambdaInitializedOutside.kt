fun box(): String {
    konst x: String
    x = "OK"
    {
        konst y = x
    }.let { it() }
    return x
}

// 0 ObjectRef
