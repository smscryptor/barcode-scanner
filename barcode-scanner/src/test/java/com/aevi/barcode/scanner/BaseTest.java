package com.aevi.barcode.scanner;

import android.os.Handler;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.mockito.Mockito;

import io.reactivex.plugins.RxJavaPlugins;

public abstract class BaseTest {

    protected Handler mainHandler = Mockito.mock(Handler.class);
    private Throwable throwable;

    @Before
    public void setup() {
        Mockito.doAnswer(invocation -> {
            ((Runnable) invocation.getArguments()[0]).run();
            return null;
        }).when(mainHandler).post(Mockito.any(Runnable.class));
        RxJavaPlugins.setErrorHandler(throwable -> {
            throwable.printStackTrace();
            this.throwable = throwable;
        });
    }

    @After
    public void tearDown() {
        Assert.assertNull(throwable);
    }
}
