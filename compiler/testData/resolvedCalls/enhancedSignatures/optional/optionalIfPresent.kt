import java.util.*

fun use(v: Optional<String>) {
    v.<caret>ifPresent { konstue ->  }
}

fun use2(v: Optional<String?>) {
    v.<caret>ifPresent { konstue ->  }
}
