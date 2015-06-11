package com.voronnenok.flyhttp.mock;

import android.content.Context;
import android.test.mock.MockContext;

import java.io.File;

/**
 * Created by voronnenok on 09.06.15.
 */
public class TestContext extends MockContext {
    @Override
    public File getExternalCacheDir() {
        return new File("data");
    }

    @Override
    public Context getApplicationContext() {
        return this;

    }
}
