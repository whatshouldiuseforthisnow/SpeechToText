package org.example;

import okhttp3.*;


import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RoomBooking {
    private final OkHttpClient client;
    private String accessToken;

    public RoomBooking(){
        client = new OkHttpClient();
    }




    public boolean login(String email, String password, String url) throws Exception{
        JSONObject json = new JSONObject();
        json.put("email",email);
        json.put("password",password);

        RequestBody body = RequestBody.create(json.toString(),
                MediaType.parse("application/json"));


        Request request = new Request.Builder().url(url + "/api/v1/member/login/")
                .post(body).build();
        System.out.println("logging in with: ");
        System.out.println("Url:"+url);


        try(Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                if (response.body() != null) {
                    String errorBody = response.body().string();
                    System.err.println("error" + errorBody);
                }
                return false;
            }
            String respBody = response.body().string();
            JSONObject respJson = new JSONObject(respBody);
            if (respJson.has("token")) {
                JSONObject tokenObj = respJson.getJSONObject("token");
                if (tokenObj.has("access")) {
                    accessToken = tokenObj.getString("access");
                    return true;
                } else {
                    System.err.println("token oBject is wack"+tokenObj.toString());
                    return false;
                }
            }
                else {
                    System.err.println("wack login method");
                    return false;

            }
        }


    }

    private Request.Builder authRequest(String url){
        return new Request.Builder()
                .url(url)
                .addHeader("Authorization","Bearer " + accessToken);
    }


    public String listAvailableRooms(String baseUrl, String startTime, String endTime) throws Exception{
        HttpUrl.Builder urlbuilder = HttpUrl.parse(
                baseUrl + "/api/v1/meeting-rooms/available")
                .newBuilder();

        if (startTime!=null){
            urlbuilder.addQueryParameter("start_time",startTime);
        }
        if (endTime!=null){
            urlbuilder.addQueryParameter("end_time",endTime);
        }


        Request request = authRequest(urlbuilder.build().toString())
                .get()
                .build();

        try (Response response = client.newCall(request).execute()){
            return response.body().string();
        }
    }

    public String bookRoom(String baseUrl, int roomId, LocalDateTime startTime, LocalDateTime endTime, int noOfPersons) throws Exception{
        JSONObject json = new JSONObject();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");
        json.put("start_time", startTime.format(formatter));
        json.put("end_time", endTime.format(formatter));
        json.put("no_of_persons", noOfPersons);

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json")
        );

        Request request = authRequest(baseUrl+"/api/v1/meeting-rooms/" + roomId + "/book/")
                .post(body)
                .build();
        try(Response response = client.newCall(request).execute()){
            return response.body().string();
        }
    }

    public String listMyBooking(String baseUrl) throws Exception{
        Request request = authRequest(baseUrl + "/api/v1/meeting-rooms/my-bookings/")
                .get()
                .build();
        try(Response response = client.newCall(request).execute()){
            return response.body().string();
        }
    }

    public String cancelBooking(String baseUrl, int roomID) throws Exception{
        Request request = authRequest(baseUrl + "/api/v1/meeting-rooms/" + roomID+"/cancel-booking/")
                .delete()
                .build();

        try(Response response = client.newCall(request).execute()){
            if(response.code() == 204 || response.isSuccessful()){
                return "cancelled";
            }
            String errorBody = response.body() != null ?
                    response.body().string() : "unknown error";
            return "error: "+ response.code() + " "+ errorBody;
        }
    }


}
