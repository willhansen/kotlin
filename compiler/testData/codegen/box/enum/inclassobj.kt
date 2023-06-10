fun box() = if(Context.operatingSystemType == Context.Companion.OsType.OTHER) "OK" else "fail"

public class Context
{
        companion object
        {
                public enum class OsType {
                        LINUX,
                        OTHER;
                }

                public konst operatingSystemType: OsType
                        get() = OsType.OTHER
        }
}
