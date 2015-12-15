package com.carrus.trucker.models;

/**
 * Created by Saurbhv on 10/31/15.
 */
public class ExpandableChildItem {
    String name;
    String detail;


    public ExpandableChildItem(String name,String detail) {
        this.name=name;
        this.detail=detail;

    }


    public String getDetail() {

        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
