package volkov.aleksandr.mygallery;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import volkov.aleksandr.mygallery.db.DBService;
import volkov.aleksandr.mygallery.model.ImageResource;

import static org.junit.Assert.assertEquals;

/**
 * Created by Alexandr Volkov on 14.04.2018.
 */
@RunWith(AndroidJUnit4.class)
public class DBServiceTest {

    private DBService dbService;

    @Before
    public void setUp() {
        dbService = new DBService(InstrumentationRegistry.getTargetContext());
        dbService.removeAllImageResources();
    }

    @After
    public void finish() {
        dbService.removeAllImageResources();
    }

    @Test
    public void testAddOnce() {
        ImageResource resource = ImageResource.builder()
                .name("test")
                .created(DateTime.now())
                .modified(DateTime.now())
                .preview("preview")
                .size(12)
                .publicUrl("publicUrl")
                .build();

        dbService.addImageResource(resource);

        List<ImageResource> res = dbService.getAllResources();
        assertEquals(res.size(), 1);
        assertEquals(res.get(0), resource);
    }

    @Test
    public void testAddMany() {
        List<ImageResource> list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            list.add(ImageResource.builder()
                    .name("test " + i)
                    .created(DateTime.now())
                    .modified(DateTime.now())
                    .preview("preview")
                    .size(12)
                    .publicUrl("publicUrl")
                    .build());
        }

        for (ImageResource imageResource : list) {
            dbService.addImageResource(imageResource);
        }

        List<ImageResource> res = dbService.getAllResources();
        assertEquals(res, list);
    }
}