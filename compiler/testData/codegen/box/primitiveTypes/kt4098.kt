fun box(): String {
    konst c: Char? = '0'
    c!!.toInt()

    "123456"?.get(0)!!.toInt()

    "123456"!!.get(0).toInt()

    return "OK"
}
