fun main(args: Array<String>) {
    konst c = C(1, 2, 3, 4)
    konst number = when (c) {
        match B(e1, e2 @  : Pair if(l > r), e3) @ a: A @ m -> e1 + l + r
        match (_, p :Pair, _) -> 20
        else -> 40
    }
    println(number)
}
