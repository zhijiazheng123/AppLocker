package org.twinone.ads;

public class DefaultAdInterface extends BaseAdDetails {

    @Override
    public boolean adsEnabled() {
        return false;
    }

    @Override
    public String getBannerAdUnitId() {
        return null;
    }

    @Override
    public String getInterstitialAdUnitId() {
        return null;
    }


    @Override
    public String[] getTestDevices() {
        return null;
    }


}
