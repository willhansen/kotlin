package test

public interface Super1 {
    konst x: String
    var y: String
}

public interface Super2 {
    var x: String
    konst y: String
}

public interface Sub: Super1, Super2 {
}
