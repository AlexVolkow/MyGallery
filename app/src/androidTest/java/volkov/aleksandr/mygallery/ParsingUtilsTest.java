package volkov.aleksandr.mygallery;

import android.support.test.runner.AndroidJUnit4;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import volkov.aleksandr.mygallery.model.ImageResource;
import volkov.aleksandr.mygallery.network.ParsingUtils;
import volkov.aleksandr.mygallery.utils.DateHelper;

import static org.junit.Assert.assertEquals;

/**
 * Created by Alexandr Volkov on 14.04.2018.
 */
@RunWith(AndroidJUnit4.class)
public class ParsingUtilsTest {

    @Test
    public void normalImageResource() throws JSONException {
        JSONObject json = new JSONObject("{\n" +
                "        \"public_key\": \"bU7NCvY9sf+va7XmmDKB37eegV1Z8oCmi/JWU1xsXrI=\",\n" +
                "        \"name\": \"cats_31.jpg\",\n" +
                "        \"created\": \"2018-04-07T14:41:07+00:00\",\n" +
                "        \"public_url\": \"https://yadi.sk/i/TobVJtKW3UHmH2\",\n" +
                "        \"modified\": \"2018-04-11T14:26:16+00:00\",\n" +
                "        \"preview\": \"https://downloader.disk.yandex.ru/preview/0c1ead55d9820a365677fc2befde11d95c8a359aac4f848fdabcb384525bfe8a/5ad25a47/-U7XQc8NSyGy5DKyFRKKRkFVHUxgXmlJ0qXubosE0y171Mo1-bjXPzRV9jfv5ISl-BUfZvfwazXEDx9guMWhdQ%3D%3D?uid=0&filename=cats_31.jpg&disposition=inline&hash=&limit=0&content_type=image%2Fjpeg&tknv=v2&size=S&crop=0\",\n" +
                "        \"size\": 70660\n" +
                "      }");
        ImageResource imageResource = ParsingUtils.parseImageResource(json);
        assertEquals(imageResource.getName(), "cats_31.jpg");
        assertEquals(imageResource.getPublicUrl(), "https://yadi.sk/i/TobVJtKW3UHmH2");
        assertEquals(imageResource.getSize(), 70660);
        assertEquals(imageResource.getCreated(), DateHelper.parseTime("2018-04-07T14:41:07+00:00"));
        assertEquals(imageResource.getModified(), DateHelper.parseTime("2018-04-11T14:26:16+00:00"));
        assertEquals(imageResource.getPreview(), "https://downloader.disk.yandex.ru/preview/0c1ead55d9820a365677fc2befde11d95c8a359aac4f848fdabcb384525bfe8a/5ad25a47/-U7XQc8NSyGy5DKyFRKKRkFVHUxgXmlJ0qXubosE0y171Mo1-bjXPzRV9jfv5ISl-BUfZvfwazXEDx9guMWhdQ%3D%3D?uid=0&filename=cats_31.jpg&disposition=inline&hash=&limit=0&content_type=image%2Fjpeg&tknv=v2&size=S&crop=0");
    }

    @Test(expected = JSONException.class)
    public void noFieldImageResource() throws JSONException {
        JSONObject json = new JSONObject("{\n" +
                "        \"public_key\": \"bU7NCvY9sf+va7XmmDKB37eegV1Z8oCmi/JWU1xsXrI=\",\n" +
                "        \"name\": \"cats_31.jpg\",\n" +
                "        \"created\": \"2018-04-07T14:41:07+00:00\",\n" +
                "        \"preview\": \"https://downloader.disk.yandex.ru/preview/0c1ead55d9820a365677fc2befde11d95c8a359aac4f848fdabcb384525bfe8a/5ad25a47/-U7XQc8NSyGy5DKyFRKKRkFVHUxgXmlJ0qXubosE0y171Mo1-bjXPzRV9jfv5ISl-BUfZvfwazXEDx9guMWhdQ%3D%3D?uid=0&filename=cats_31.jpg&disposition=inline&hash=&limit=0&content_type=image%2Fjpeg&tknv=v2&size=S&crop=0\",\n" +
                "        \"size\": 70660\n" +
                "      }");
        ParsingUtils.parseImageResource(json);
    }

    @Test
    public void parseError() throws JSONException {
        JSONObject json = new JSONObject("{\n" +
                "  \"message\": \"Ошибка сервера.\",\n" +
                "  \"description\": \"Internal Server Error\",\n" +
                "  \"error\": \"InternalServerError\"\n" +
                "}");

        String error = ParsingUtils.parseError(json);
        assertEquals(error, "InternalServerError: Internal Server Error");
    }

    @Test
    public void parseImageResourceList() throws JSONException {
        JSONObject json = new JSONObject("{\n" +
                "  \"public_key\": \"bU7NCvY9sf+va7XmmDKB37eegV1Z8oCmi/JWU1xsXrI=\",\n" +
                "  \"_embedded\": {\n" +
                "    \"items\": [\n" +
                "      {\n" +
                "        \"name\": \"cats_25.jpg\",\n" +
                "        \"created\": \"2018-04-07T14:41:05+00:00\",\n" +
                "        \"public_url\": \"https://yadi.sk/i/neaVRJfx3UHmEk\",\n" +
                "        \"modified\": \"2018-04-12T11:45:34+00:00\",\n" +
                "        \"preview\": \"https://downloader.disk.yandex.ru/preview/34d4328c1faf6899cc48767bc8b5d6874a835d068310f689e07a095586c9af05/5ad26294/uggcuTndOy5XdhLp9WtBMWzeGeYV-leK0COHs1iiv13OFiRZdgr_rmDwnGjLPchPtvIta84TxZngI7Gn5mwzAg%3D%3D?uid=0&filename=cats_25.jpg&disposition=inline&hash=&limit=0&content_type=image%2Fjpeg&tknv=v2&size=S&crop=0\",\n" +
                "        \"size\": 71631\n" +
                "      },\n" +
                "      {\n" +
                "        \"name\": \"cats_31.jpg\",\n" +
                "        \"created\": \"2018-04-07T14:41:07+00:00\",\n" +
                "        \"public_url\": \"https://yadi.sk/i/TobVJtKW3UHmH2\",\n" +
                "        \"modified\": \"2018-04-11T14:26:16+00:00\",\n" +
                "        \"preview\": \"https://downloader.disk.yandex.ru/preview/7585dc47bed5a9f009c8378fcadb61a1248181fd528a3074c37c8c7534ddb77f/5ad26294/-U7XQc8NSyGy5DKyFRKKRkFVHUxgXmlJ0qXubosE0y171Mo1-bjXPzRV9jfv5ISl-BUfZvfwazXEDx9guMWhdQ%3D%3D?uid=0&filename=cats_31.jpg&disposition=inline&hash=&limit=0&content_type=image%2Fjpeg&tknv=v2&size=S&crop=0\",\n" +
                "        \"size\": 70660\n" +
                "      }\n" +
                "    ],\n" +
                "    \"limit\": 2,\n" +
                "    \"offset\": 0,\n" +
                "    \"path\": \"/\",\n" +
                "    \"total\": 102\n" +
                "  },\n" +
                "  \"revision\": 1523375726249263\n" +
                "}");
        List<ImageResource> list = ParsingUtils.parseImageResourceList(json);
        ImageResource first = ParsingUtils.parseImageResource(new JSONObject("{\n" +
                "        \"name\": \"cats_25.jpg\",\n" +
                "        \"created\": \"2018-04-07T14:41:05+00:00\",\n" +
                "        \"public_url\": \"https://yadi.sk/i/neaVRJfx3UHmEk\",\n" +
                "        \"modified\": \"2018-04-12T11:45:34+00:00\",\n" +
                "        \"preview\": \"https://downloader.disk.yandex.ru/preview/34d4328c1faf6899cc48767bc8b5d6874a835d068310f689e07a095586c9af05/5ad26294/uggcuTndOy5XdhLp9WtBMWzeGeYV-leK0COHs1iiv13OFiRZdgr_rmDwnGjLPchPtvIta84TxZngI7Gn5mwzAg%3D%3D?uid=0&filename=cats_25.jpg&disposition=inline&hash=&limit=0&content_type=image%2Fjpeg&tknv=v2&size=S&crop=0\",\n" +
                "        \"size\": 71631\n" +
                "      }"));
        ImageResource second = ParsingUtils.parseImageResource(new JSONObject("{\n" +
                "        \"name\": \"cats_31.jpg\",\n" +
                "        \"created\": \"2018-04-07T14:41:07+00:00\",\n" +
                "        \"public_url\": \"https://yadi.sk/i/TobVJtKW3UHmH2\",\n" +
                "        \"modified\": \"2018-04-11T14:26:16+00:00\",\n" +
                "        \"preview\": \"https://downloader.disk.yandex.ru/preview/7585dc47bed5a9f009c8378fcadb61a1248181fd528a3074c37c8c7534ddb77f/5ad26294/-U7XQc8NSyGy5DKyFRKKRkFVHUxgXmlJ0qXubosE0y171Mo1-bjXPzRV9jfv5ISl-BUfZvfwazXEDx9guMWhdQ%3D%3D?uid=0&filename=cats_31.jpg&disposition=inline&hash=&limit=0&content_type=image%2Fjpeg&tknv=v2&size=S&crop=0\",\n" +
                "        \"size\": 70660\n" +
                "      }"));
        assertEquals(list.size(), 2);
        assertEquals(list.get(0), first);
        assertEquals(list.get(1), second);
    }

    @Test(expected = JSONException.class)
    public void noEmbeddedImageResourceList() throws JSONException {
        JSONObject json = new JSONObject("{\n" +
                "  \"public_key\": \"bU7NCvY9sf+va7XmmDKB37eegV1Z8oCmi/JWU1xsXrI=\"}");
        ParsingUtils.parseImageResourceList(json);
    }

    public void parseDownloadLink() throws JSONException {
        JSONObject json = new JSONObject("{\n" +
                "  \"href\": \"https://cloud-api.yandex.net/v1/disk/resources?path=disk%3A%2Ffoo%2Fphoto.png\",\n" +
                "  \"method\": \"GET\",\n" +
                "  \"templated\": false\n" +
                "}");
        String link = ParsingUtils.parseDownloadUrl(json);
        assertEquals(link, "https://cloud-api.yandex.net/v1/disk/resources?path=disk%3A%2Ffoo%2Fphoto.png");
    }
}