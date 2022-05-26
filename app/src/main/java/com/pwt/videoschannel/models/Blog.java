package com.pwt.videoschannel.models;

import java.io.Serializable;

public class Blog implements Serializable {

    public String id;
    public String blogger_id;
    public String api_key;

    public String getBlogger_id() {
        return blogger_id;
    }

    public String getApi_key() {
        return api_key;
    }
}
