package com.carrustruckerapp.interfaces;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by Saurbhv on 10/21/15.
 */
public interface WebServices {

    @GET("/api/v1/trucker/verify")
    public void verifyUser(@Header("authorization") String accessToken,
                           Callback<String> callback);

    @GET("/api/v1/trucker/checkDriveId")
    public void checkDriverId(@Query("driverId") String driverId,
                              @Query("deviceType") String deviceType,
                              @Query("deviceName") String deviceName,
                              @Query("deviceToken") String deviceToken,
                           Callback<String> callback);

    @FormUrlEncoded
    @POST("/api/v1/trucker/checkOTPDuringLogin")
    public void checkOtp(@Field("driverId") String driverId,
                         @Field("OTP") String otp,
                         Callback<String> callback);

    @PUT("/api/v1/trucker/logout")
    public void logoutDriver(@Header("authorization") String accessToken/*,@Field("authorization") String accessToken1*/,
                               Callback<String> callback);

    @GET("/api/v1/trucker/getPast")
    public void getPastOrders(@Header("authorization") String accessToken,
                           Callback<String> callback);

    @GET("/api/v1/trucker/getUpComing")
    public void getUpComingOrders(@Header("authorization") String accessToken,
                           Callback<String> callback);

    @GET("/api/v1/trucker/getBookingDetail/{bookingId}")
    public void getBookingDetails(@Header("authorization") String accessToken,@Path("bookingId") String bookingId,
                                  Callback<String> callback);

    @FormUrlEncoded
    @PUT("/api/v1/trucker/changeBookingToComplete/{bookingId}")
    public void completeOrder(@Header("authorization") String accessToken,
                              @Path("bookingId") String bookingId,
                              @Field("bookingStatus") String bookingStatus,
                             Callback<String> callback);

    @FormUrlEncoded
    @PUT("/api/v1/trucker/changeStatus/{bookingId}")
    public void changeOrderStatus(@Header("authorization") String accessToken,
                              @Path("bookingId") String bookingId,
                              @Field("bookingStatus") String bookingStatus,
                              Callback<String> callback);

    @FormUrlEncoded
    @PUT("/api/v1/trucker/addNote")
    public void addNotes(@Header("authorization") String accessToken,
                                  @Field("bookingId") String bookingId,
                                  @Field("note") String note,
                                  Callback<String> callback);

    @FormUrlEncoded
    @PUT("/api/v1/trucker/getCash/{bookingId}")
    public void collectCash(@Header("authorization") String accessToken,
                         @Path("bookingId") String bookingId,
                         @Field("paymentStatus") String paymentStatus,
                         Callback<String> callback);



}
