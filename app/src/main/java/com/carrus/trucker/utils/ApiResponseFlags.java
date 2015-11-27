package com.carrus.trucker.utils;

/**
 * Created by Saurbhv on 11/26/15.
 */
public enum ApiResponseFlags {

    OK(200),
    Created(201),
    Bad_Request(400),
    Unauthorized(401),
    Not_Found(404),
    Already_Exists(409),
    Internal_Server_Error(500);

    private int ordinal;

    private ApiResponseFlags(int ordinal) {
        this.ordinal = ordinal;
    }

    public int getOrdinal() {
        return ordinal;
    }
}
