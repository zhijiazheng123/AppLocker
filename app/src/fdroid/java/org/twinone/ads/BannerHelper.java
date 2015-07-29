package org.twinone.ads;

import android.content.Context;
import android.view.View;

public class BannerHelper extends BaseBannerHelper {
    public BannerHelper(Context c, View parent) {
    }

    @Override
    public String getAdUnitId() {
        return null;
    }

    public boolean shouldShowAds() {
        return false;
    }

    @Override
    public void loadAd() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void destroy() {
    }
}
