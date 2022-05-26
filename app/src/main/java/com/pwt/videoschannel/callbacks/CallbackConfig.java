package com.pwt.videoschannel.callbacks;

import com.pwt.videoschannel.models.Ads;
import com.pwt.videoschannel.models.App;
import com.pwt.videoschannel.models.Blog;
import com.pwt.videoschannel.models.Category;
import com.pwt.videoschannel.models.Notification;

import java.util.ArrayList;
import java.util.List;

public class CallbackConfig {

    public Blog blog = null;
    public App app = null;
    public Notification notification = null;
    public Ads ads = null;
    public List<Category> labels = new ArrayList<>();

}
