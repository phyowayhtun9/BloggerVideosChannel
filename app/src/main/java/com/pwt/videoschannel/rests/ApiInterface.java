package com.pwt.videoschannel.rests;

import com.pwt.videoschannel.callbacks.CallbackConfig;
import com.pwt.videoschannel.callbacks.CallbackLabel;
import com.pwt.videoschannel.callbacks.CallbackPost;
import com.pwt.videoschannel.callbacks.CallbackPostDetail;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface ApiInterface {

    String CACHE = "Cache-Control: max-age=0";
    String AGENT = "Data-Agent: Blogger Recipes App";

    @Headers({CACHE, AGENT})
    @GET
    Call<CallbackConfig> getJsonUrl(@Url String url);

    @Headers({CACHE, AGENT})
    @GET("uc?export=download")
    Call<CallbackConfig> getDriveJsonFileId(
            @Query("id") String id
    );

    @Headers({CACHE, AGENT})
    @GET("posts/default?alt=json")
    Call<CallbackLabel> getLabel();

    @Headers({CACHE, AGENT})
    @GET("posts")
    Call<CallbackPost> getPosts(
            @Query("orderBy") String orderBy,
            @Query("key") String key,
            @Query("maxResults") int maxResults,
            @Query("pageToken") String pageToken
    );

    @Headers({CACHE, AGENT})
    @GET(".")
    Call<CallbackPostDetail> getPostDetail(
            @Query("key") String key
    );

    @Headers({CACHE, AGENT})
    @GET("posts")
    Call<CallbackPost> getCategoryDetail(
            @Query("labels") String labels,
            @Query("orderBy") String orderBy,
            @Query("key") String key,
            @Query("pageToken") String pageToken
    );

    @Headers({CACHE, AGENT})
    @GET("posts")
    Call<CallbackPost> getRelatedPosts(
            @Query("labels") String labels,
            @Query("orderBy") String orderBy,
            @Query("key") String key
    );

    @Headers({CACHE, AGENT})
    @GET("posts/search")
    Call<CallbackPost> getSearchPosts(
            @Query("q") String q,
            @Query("key") String key
    );

}