package test

public interface Super1 {
    konst x: String
    konst y: CharSequence
}

public interface Super2 {
    konst x: CharSequence
    konst y: String
}

public interface Sub: Super1, Super2 {
}
