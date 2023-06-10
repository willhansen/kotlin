fun main() {
    for ((a) in b) {}
    for ((a, b) in b) {}
    for ((a: Int, b: Int) in b) {}
    for ((a: Int, b) in b) {}
    for ((a, b: Int) in b) {}

    for (konst (a) in b) {}
    for (konst (a, b) in b) {}
    for (konst (a: Int, b: Int) in b) {}
    for (konst (a: Int, b) in b) {}
    for (konst (a, b: Int) in b) {}

    for (var (a) in b) {}
    for (var (a, b) in b) {}
    for (var (a: Int, b: Int) in b) {}
    for (var (a: Int, b) in b) {}
    for (var (a, b: Int) in b) {}

    for ((a in b) {}
    for ((a, ) in b) {}
    for ((a: ) in b) {}
    for ((a: , ) in b) {}
    for ((, b: Int) in b) {}

    for ((a: in b) {}
    for (( ) {}
}