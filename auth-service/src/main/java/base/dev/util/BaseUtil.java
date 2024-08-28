package base.dev.util;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class BaseUtil {

    public static String generateRandomCode(){

        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public static boolean isEmpty(Collection<?> collection, Map<?, ?> dataMap, int type) {
        switch (type) {
            case 1:
                return Objects.isNull(collection) || collection.isEmpty();
            case 2:
                return Objects.isNull(dataMap) || dataMap.isEmpty();
            default:
                throw new IllegalArgumentException("Invalid type: " + type);
        }
    }
}
