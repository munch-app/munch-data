package munch.data.linking;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

/**
 * Created by: Fuxing
 * Date: 26/3/2018
 * Time: 6:27 AM
 * Project: munch-data
 */
public class UberEatsPlatform implements Platform {

    @Override
    public String parse(PlatformUrl url) {
        if (url.hasPath(2, "stores")) {
            return wrap("ubereats.com/stores/", url.getPath(1));
        }

        if (url.hasPath(4) && url.getPath(1).equals("food-delivery")) {
            return wrap("ubereats.com/stores/", decodeBase64(url.getPath(3)));
        }

        return null;
    }

    public static void main(String[] args) {

        UUID uuid = UUID.fromString("2a6866e2-7ddd-4b5c-9c73-f4acfb82f9bf");

        ByteBuffer uuidBytes = ByteBuffer.wrap(new byte[16]);
        uuidBytes.putLong(uuid.getMostSignificantBits());
        uuidBytes.putLong(uuid.getLeastSignificantBits());

        System.out.println(java.util.Base64.getUrlEncoder().encodeToString(uuidBytes.array()));

        System.out.println(decodeBase64("Kmhm4n3dS1ycc_Ss-4L5vw"));
    }

    private static String decodeBase64(String base64) {
        if (base64 == null) return null;
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(base64);
            ByteBuffer bb = ByteBuffer.wrap(decoded);
            return new UUID(bb.getLong(), bb.getLong()).toString();
        }catch (IllegalArgumentException e) {
            return null;
        }
    }
}
