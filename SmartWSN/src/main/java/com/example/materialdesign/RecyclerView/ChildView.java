package com.example.materialdesign.RecyclerView;

/**
 * Created by B on 2018/3/1.
 */

public class ChildView {
    private String sensor;

    public ChildView(String name) {
        this.sensor = name;
    }
    public String getName() {
        return sensor;
    }

    public void setName(String name) {
        this.sensor = name;
    }
}
