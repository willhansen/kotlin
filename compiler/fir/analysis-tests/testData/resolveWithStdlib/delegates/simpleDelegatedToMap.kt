class C(konst map: MutableMap<String, Any>) {
    var foo by map
}

var bar by hashMapOf<String, Any>()