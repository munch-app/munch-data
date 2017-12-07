package munch.data.structure;

import munch.restful.core.JsonUtils;
import org.junit.jupiter.api.Test;

/**
 * Created by: Fuxing
 * Date: 8/12/2017
 * Time: 6:43 AM
 * Project: munch-data
 */
class PlaceTest {

    static final String oldPlace = "{\n" +
            "    \"id\": \"3668fa18-5bae-429d-905e-85a5e40879d6\",\n" +
            "    \"name\": \"Alakai\",\n" +
            "    \"phone\": \"+65 6904 4957\",\n" +
            "    \"website\": \"http://www.alakaicompany.com\",\n" +
            "    \"description\": \"\uD83C\uDF4DORIGINAL Hawaiian-style POKE\uD83C\uDF4D Made FRESH to ORDER \uD83C\uDF4D\",\n" +
            "    \"location\": {\n" +
            "        \"street\": \"Neil Road\",\n" +
            "        \"address\": \"3 Everton Park #01-79, Singapore 080003\",\n" +
            "        \"nearestTrain\": \"Outram Park\",\n" +
            "        \"city\": \"Singapore\",\n" +
            "        \"country\": \"Singapore\",\n" +
            "        \"postal\": \"080003\",\n" +
            "        \"latLng\": \"1.2773123212269,103.83843898773\"\n" +
            "    },\n" +
            "    \"review\": {\n" +
            "        \"total\": 40,\n" +
            "        \"average\": 5\n" +
            "    },\n" +
            "    \"tag\": {\n" +
            "        \"explicits\": [\n" +
            "            \"restaurant\"\n" +
            "        ],\n" +
            "        \"implicits\": [\n" +
            "            \"bukit merah\",\n" +
            "            \"outram\",\n" +
            "            \"everton park\",\n" +
            "            \"breakfast\",\n" +
            "            \"lunch\"\n" +
            "        ]\n" +
            "    },\n" +
            "    \"hours\": [\n" +
            "        {\n" +
            "            \"day\": \"thu\",\n" +
            "            \"open\": \"11:30\",\n" +
            "            \"close\": \"20:30\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"day\": \"wed\",\n" +
            "            \"open\": \"11:30\",\n" +
            "            \"close\": \"20:30\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"day\": \"fri\",\n" +
            "            \"open\": \"11:30\",\n" +
            "            \"close\": \"20:30\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"day\": \"sat\",\n" +
            "            \"open\": \"11:30\",\n" +
            "            \"close\": \"20:30\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"day\": \"tue\",\n" +
            "            \"open\": \"11:30\",\n" +
            "            \"close\": \"20:30\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"images\": [\n" +
            "        {\n" +
            "            \"weight\": 1,\n" +
            "            \"source\": \"facebook\",\n" +
            "            \"images\": {\n" +
            "                \"320x320\": \"https://s3-ap-southeast-1.amazonaws.com/munch-facebook/4512cf91cfbfc290fd0f3c7da23cdcac6199f070ed175914c0943396392e07fb_320x320.jpg\",\n" +
            "                \"original\": \"https://s3-ap-southeast-1.amazonaws.com/munch-facebook/4512cf91cfbfc290fd0f3c7da23cdcac6199f070ed175914c0943396392e07fb.jpg\",\n" +
            "                \"150x150\": \"https://s3-ap-southeast-1.amazonaws.com/munch-facebook/4512cf91cfbfc290fd0f3c7da23cdcac6199f070ed175914c0943396392e07fb_150x150.jpg\"\n" +
            "            }\n" +
            "        },\n" +
            "        {\n" +
            "            \"weight\": 1,\n" +
            "            \"source\": \"article\",\n" +
            "            \"images\": {\n" +
            "                \"320x320\": \"https://s3-ap-southeast-1.amazonaws.com/munch-article/effa3c2034fea7ed499667e2f03f49853737b2ed002a9777e773354cdf8a2ccb_320x320.jpg\",\n" +
            "                \"original\": \"https://s3-ap-southeast-1.amazonaws.com/munch-article/effa3c2034fea7ed499667e2f03f49853737b2ed002a9777e773354cdf8a2ccb.jpg\",\n" +
            "                \"150x150\": \"https://s3-ap-southeast-1.amazonaws.com/munch-article/effa3c2034fea7ed499667e2f03f49853737b2ed002a9777e773354cdf8a2ccb_150x150.jpg\"\n" +
            "            }\n" +
            "        },\n" +
            "        {\n" +
            "            \"weight\": 1,\n" +
            "            \"source\": \"article\",\n" +
            "            \"images\": {\n" +
            "                \"320x320\": \"https://s3-ap-southeast-1.amazonaws.com/munch-article/8c69b07b14c0940a7af203ca5e683bf0a1dc0ee3231312143f7473b862785859_320x320.jpg\",\n" +
            "                \"original\": \"https://s3-ap-southeast-1.amazonaws.com/munch-article/8c69b07b14c0940a7af203ca5e683bf0a1dc0ee3231312143f7473b862785859.jpg\",\n" +
            "                \"640x640\": \"https://s3-ap-southeast-1.amazonaws.com/munch-article/8c69b07b14c0940a7af203ca5e683bf0a1dc0ee3231312143f7473b862785859_640x640.jpg\",\n" +
            "                \"150x150\": \"https://s3-ap-southeast-1.amazonaws.com/munch-article/8c69b07b14c0940a7af203ca5e683bf0a1dc0ee3231312143f7473b862785859_150x150.jpg\"\n" +
            "            }\n" +
            "        }\n" +
            "    ],\n" +
            "    \"createdDate\": 1508114931365,\n" +
            "    \"updatedDate\": 1512658526842,\n" +
            "    \"ranking\": 1063,\n" +
            "    \"dataType\": \"Place\"\n" +
            "}";

    static final String newPlace = "{\n" +
            "    \"id\": \"3668fa18-5bae-429d-905e-85a5e40879d6\",\n" +
            "    \"name\": \"Alakai\",\n" +
            "    \"phone\": \"+65 6904 4957\",\n" +
            "    \"website\": \"http://www.alakaicompany.com\",\n" +
            "    \"description\": \"\uD83C\uDF4DORIGINAL Hawaiian-style POKE\uD83C\uDF4D Made FRESH to ORDER \uD83C\uDF4D\",\n" +
            "    \"location\": {\n" +
            "        \"street\": \"Neil Road\",\n" +
            "        \"address\": \"3 Everton Park #01-79, Singapore 080003\",\n" +
            "        \"nearestTrain\": \"Outram Park\",\n" +
            "        \"city\": \"Singapore\",\n" +
            "        \"country\": \"Singapore\",\n" +
            "        \"postal\": \"080003\",\n" +
            "        \"latLng\": \"1.2773123212269,103.83843898773\"\n" +
            "    },\n" +
            "    \"review\": {\n" +
            "        \"total\": 40,\n" +
            "        \"average\": 5\n" +
            "    },\n" +
            "    \"tag\": {\n" +
            "        \"explicits\": [\n" +
            "            \"restaurant\"\n" +
            "        ],\n" +
            "        \"implicits\": [\n" +
            "            \"bukit merah\",\n" +
            "            \"outram\",\n" +
            "            \"everton park\",\n" +
            "            \"breakfast\",\n" +
            "            \"lunch\"\n" +
            "        ]\n" +
            "    },\n" +
            "    \"hours\": [\n" +
            "        {\n" +
            "            \"day\": \"thu\",\n" +
            "            \"open\": \"11:30\",\n" +
            "            \"close\": \"20:30\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"day\": \"wed\",\n" +
            "            \"open\": \"11:30\",\n" +
            "            \"close\": \"20:30\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"day\": \"fri\",\n" +
            "            \"open\": \"11:30\",\n" +
            "            \"close\": \"20:30\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"day\": \"sat\",\n" +
            "            \"open\": \"11:30\",\n" +
            "            \"close\": \"20:30\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"day\": \"tue\",\n" +
            "            \"open\": \"11:30\",\n" +
            "            \"close\": \"20:30\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"images\": [\n" +
            "        {\n" +
            "            \"weight\": 1,\n" +
            "            \"source\": \"facebook\",\n" +
            "            \"images\": {\n" +
            "                \"320x320\": \"https://s3-ap-southeast-1.amazonaws.com/munch-facebook/4512cf91cfbfc290fd0f3c7da23cdcac6199f070ed175914c0943396392e07fb_320x320.jpg\",\n" +
            "                \"original\": \"https://s3-ap-southeast-1.amazonaws.com/munch-facebook/4512cf91cfbfc290fd0f3c7da23cdcac6199f070ed175914c0943396392e07fb.jpg\",\n" +
            "                \"150x150\": \"https://s3-ap-southeast-1.amazonaws.com/munch-facebook/4512cf91cfbfc290fd0f3c7da23cdcac6199f070ed175914c0943396392e07fb_150x150.jpg\"\n" +
            "            }\n" +
            "        },\n" +
            "        {\n" +
            "            \"weight\": 1,\n" +
            "            \"source\": \"article\",\n" +
            "            \"images\": {\n" +
            "                \"320x320\": \"https://s3-ap-southeast-1.amazonaws.com/munch-article/effa3c2034fea7ed499667e2f03f49853737b2ed002a9777e773354cdf8a2ccb_320x320.jpg\",\n" +
            "                \"original\": \"https://s3-ap-southeast-1.amazonaws.com/munch-article/effa3c2034fea7ed499667e2f03f49853737b2ed002a9777e773354cdf8a2ccb.jpg\",\n" +
            "                \"150x150\": \"https://s3-ap-southeast-1.amazonaws.com/munch-article/effa3c2034fea7ed499667e2f03f49853737b2ed002a9777e773354cdf8a2ccb_150x150.jpg\"\n" +
            "            }\n" +
            "        },\n" +
            "        {\n" +
            "            \"weight\": 1,\n" +
            "            \"source\": \"article\",\n" +
            "            \"images\": {\n" +
            "                \"320x320\": \"https://s3-ap-southeast-1.amazonaws.com/munch-article/8c69b07b14c0940a7af203ca5e683bf0a1dc0ee3231312143f7473b862785859_320x320.jpg\",\n" +
            "                \"original\": \"https://s3-ap-southeast-1.amazonaws.com/munch-article/8c69b07b14c0940a7af203ca5e683bf0a1dc0ee3231312143f7473b862785859.jpg\",\n" +
            "                \"640x640\": \"https://s3-ap-southeast-1.amazonaws.com/munch-article/8c69b07b14c0940a7af203ca5e683bf0a1dc0ee3231312143f7473b862785859_640x640.jpg\",\n" +
            "                \"150x150\": \"https://s3-ap-southeast-1.amazonaws.com/munch-article/8c69b07b14c0940a7af203ca5e683bf0a1dc0ee3231312143f7473b862785859_150x150.jpg\"\n" +
            "            }\n" +
            "        }\n" +
            "    ],\n" +
            "    \"createdDate\": 1508114931365,\n" +
            "    \"updatedDate\": 1512686282778,\n" +
            "    \"ranking\": 1063,\n" +
            "    \"dataType\": \"Place\"\n" +
            "}";

    @Test
    void checkEquals() throws Exception {
        Place oldP = JsonUtils.objectMapper.readValue(oldPlace, Place.class);
        Place newP = JsonUtils.objectMapper.readValue(newPlace, Place.class);
        System.out.println(oldP.equals(newP));
    }
}