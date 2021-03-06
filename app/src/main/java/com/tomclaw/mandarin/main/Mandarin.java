package com.tomclaw.mandarin.main;

import android.app.Application;

import com.tomclaw.mandarin.core.BitmapCache;
import com.tomclaw.mandarin.core.GlideProvider;
import com.tomclaw.mandarin.im.StatusUtil;
import com.tomclaw.mandarin.im.icq.IcqAccountRoot;
import com.tomclaw.mandarin.im.icq.IcqStatusCatalogue;
import com.tomclaw.mandarin.util.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: Solkin
 * Date: 04.11.13
 * Time: 14:08
 */
public class Mandarin extends Application {

    @Override
    public void onCreate() {
        long time = System.currentTimeMillis();
        GlideProvider.getInstance().init(this);
        StatusUtil.include(IcqAccountRoot.class.getName(), new IcqStatusCatalogue(this));
        BitmapCache.getInstance().init(this);
        super.onCreate();
        Logger.log("application start time: " + (System.currentTimeMillis() - time));
    }
}
