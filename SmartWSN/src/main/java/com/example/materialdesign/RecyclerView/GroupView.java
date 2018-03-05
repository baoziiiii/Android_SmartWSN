package com.example.materialdesign.RecyclerView;

/**
 * Created by MSI on 2018/3/1.
 */

public class GroupView {
    private String name;
    private int resId;

    public GroupView(String name, int resId) {
        this.name = name;
        this.resId = resId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }
}
