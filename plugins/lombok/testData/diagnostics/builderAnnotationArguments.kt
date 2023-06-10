// FIR_IDENTICAL
// FILE: User.java

import lombok.Builder;
import lombok.Data;

@Builder(
    builderClassName = "SpecialUserBuilder",
    buildMethodName = "execute",
    builderMethodName = "createBuilder",
    toBuilder = true,
    access = AccessLevel.PACKAGE,
    setterPrefix = "set"
)
@Data
public class User {
  @Builder.Default private int created = 0;
  private String name;
  private int age;
}


// FILE: test.kt

fun test() {
    konst userBuilder: User.SpecialUserBuilder = User.createBuilder()
    konst user = userBuilder
        .setCreated(10)
        .setName("John")
        .setAge(42)
        .execute()
}

fun testToBuilder(user: User) {
    konst userBuilder: User.SpecialUserBuilder = user.toBuilder()
}
