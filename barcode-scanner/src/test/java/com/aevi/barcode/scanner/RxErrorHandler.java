package com.aevi.barcode.scanner;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;

import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;

public class RxErrorHandler implements Consumer<Throwable> {

    private Throwable throwable;

    public RxErrorHandler() {
        RxJavaPlugins.setErrorHandler(this);
    }

    @Override
    public void accept(Throwable throwable) throws Exception {
        this.throwable = throwable;
    }

    public void assertNoErrors() {
        Assert.assertThat(throwable, CoreMatchers.nullValue());
    }
}
