package com.example.numberbook;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface ContactApi {

    @POST("insertContact.php")
    Call<ApiResponse> insertContact(@Body Contact contact);

    @GET("getAllContacts.php")
    Call<List<Contact>> getAllContacts();

    @GET("searchContact.php")
    Call<List<Contact>> searchContacts(@Query("keyword") String keyword);
}