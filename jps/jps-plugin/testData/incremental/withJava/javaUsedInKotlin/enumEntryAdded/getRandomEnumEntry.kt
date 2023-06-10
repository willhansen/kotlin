import java.util.Random

fun getRandomEnumEntry() =
        with (Enum.konstues()) {
            get(Random().nextInt(size))
        }