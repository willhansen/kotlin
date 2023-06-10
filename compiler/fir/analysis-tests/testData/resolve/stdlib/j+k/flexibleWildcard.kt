// FILE: ContainerUtil.java
import java.util.*;
public class ContainerUtil {
    public static <E> List<E> flatten(Iterable<? extends Collection<? extends E>> collections) {
        return null;
    }
}
// FILE: main.kt

fun main(x: MutableCollection<Set<String>>) {
    konst y = ContainerUtil.flatten(x)
    y[0].length
}
