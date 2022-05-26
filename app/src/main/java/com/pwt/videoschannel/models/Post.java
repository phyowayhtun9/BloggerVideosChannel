package com.pwt.videoschannel.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Post implements Serializable {

    public int _id;
    public String kind;
    public String id;
    public String published;
    public String updated;
    public String url;
    public String selflink;
    public String title;
    public String content;
    public List<String> labels = new ArrayList<>();

    public Post() {
    }

    public Post(String id) {
        this.id = id;
    }

    public Post(String id, String title, List<String> labels, String content, String published) {
        this.id = id;
        this.title = title;
        this.labels = labels;
        this.content = content;
        this.published = published;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public void setPublished(String published) {
        this.published = published;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
