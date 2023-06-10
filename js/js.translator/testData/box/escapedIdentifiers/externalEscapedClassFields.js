function A() {
    this["@inkonstid @ konst@"] = 23
    this["--inkonstid-var"] = "A: before"
}

A.prototype["get something$weird"] = function() {
    return "something weird"
}

A["static konst"] = 42
A["static var"] = "Companion: before"

A["get 🦄"] = function() {
    return "🦄"
}