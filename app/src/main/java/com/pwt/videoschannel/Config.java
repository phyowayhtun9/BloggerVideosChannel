package com.pwt.videoschannel;

import com.pwt.videoschannel.utils.Constant;

public class Config {

    //generate your access key using the link below or check the documentation for more detailed instructions
    //https://services.solodroid.co.id/access-key/generate
    public static final String ACCESS_KEY = "WVVoU01HTklUVFpNZVRsclkyMXNNbHBUTlc1aU1qbHVZa2RWZFZreU9YUk1NbHB3WWtkVmRscERPSGhOYTBaWFV6Sk5OVnB0Vm01VE0yOTZWbFJzV2xkR2NFcFRNbEo0VDBkd1ZHTnFXa3RSYlRWc1ZHcGpkbVJ0Ykd4a01UbG9ZMGhDYzJGWFRtaGtSMngyWW10c2ExZ3lUblppVXpWb1kwaEJkVmx0ZUhaYU1tUnNZMjVhY0ZwSFZuWmpNazV2V1ZjMWRWcFhkejA9";

    //default video columns count for the first time launch, supported value : Constant.VIDEO_LIST_DEFAULT or Constant.VIDEO_LIST_COMPACT
    public static final int DEFAULT_VIDEO_LIST_VIEW_TYPE = Constant.VIDEO_LIST_DEFAULT;

    //label sorting, supported value : Constant.LABEL_NAME_ASCENDING, Constant.LABEL_NAME_DESCENDING or Constant.LABEL_DEFAULT
    public static final String LABELS_SORTING = Constant.LABEL_NAME_ASCENDING;

    //category columns count, supported value : Constant.CATEGORY_ONE_COLUMNS or Constant.CATEGORY_TWO_COLUMNS
    public static final int DEFAULT_CATEGORY_LIST_VIEW_TYPE = Constant.CATEGORY_GRID_3;

    //RTL direction, e.g : for Arabic Language
    public static final boolean ENABLE_RTL_MODE = false;

    //enable copy text in the content description
    public static final boolean ENABLE_TEXT_SELECTION = false;

    //set video player orientation always is landscape mode
    public static final boolean FORCE_PLAYER_TO_LANDSCAPE = false;

    //set true if you want to display related video in the video description
    public static final boolean DISPLAY_RELATED_VIDEOS = true;

    //GDPR EU Consent
    public static final boolean LEGACY_GDPR = false;

    //Ad Placement in the particular screen, 1 to enable and 0 to disable
    public static final int BANNER_HOME = 1;
    public static final int BANNER_VIDEO_DETAIL = 1;
    public static final int BANNER_CATEGORY_DETAIL = 1;
    public static final int BANNER_SEARCH = 1;
    public static final int INTERSTITIAL_VIDEO_LIST = 1;
    public static final int NATIVE_AD_VIDEO_LIST = 1;
    public static final int NATIVE_AD_VIDEO_DETAIL = 1;

}