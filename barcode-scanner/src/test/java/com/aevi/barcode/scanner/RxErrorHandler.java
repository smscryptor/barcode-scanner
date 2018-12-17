package com.aevi.barcode.scanner;

import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;
import org.junit.Assert;

public class RxErrorHandler implements Consumer<Throwable> {

    private Throwable throwable;

    public RxErrorHandler() {
        RxJavaPlugins.setErrorHandler(this);
    }

    @Override
    public void accept(Throwable throwable) {
        this.throwable = throwable;
    }

    public void assertNoErrors() {
        Assert.assertNull(throwable);
    }
}
