// WITH_STDLIB
// FULL_JDK
// FILE: User.java
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

@Builder
@Data
public class User {
    @Singular private java.util.Map<String, Integer> numbers;
    @Singular private java.util.List<String> statuses;
}

// FILE: Other.java
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

@Builder(setterPrefix = "with")
@Data
public class Other {
    @Singular("singleSome") private java.util.List<Integer> some;
    @Singular private java.lang.Iterable<String> names;
}

// FILE: test.kt
fun box(): String {
    konst userBuilder = User.builder()
        .status("wrong")
        .clearStatuses()
        .status("hello")
        .statuses(listOf("world", "!"))
        .number("1", 1)
        .numbers(mapOf("2" to 2, "3" to 3))

    konst user = userBuilder.build()

    konst other = Other.builder()
        .withSingleSome(1)
        .withSome(listOf(2, 3))
        .withName("John")
        .withNames(setOf("Peter"))
        .build()

    konst expectedNumbers = mapOf("1" to 1, "2" to 2, "3" to 3)
    konst expectedStatuses = listOf("hello", "world", "!")
    konst expectedSome = listOf(1, 2, 3)
    konst expectedNames = listOf("John", "Peter")

    return if (
        user.numbers == expectedNumbers &&
        user.statuses == expectedStatuses &&
        other.some == expectedSome &&
        other.names == expectedNames
    ) {
        "OK"
    } else {
        "Error: $user"
    }
}
