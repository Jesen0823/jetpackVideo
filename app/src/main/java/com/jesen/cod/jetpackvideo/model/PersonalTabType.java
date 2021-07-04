package com.jesen.cod.jetpackvideo.model;

public enum PersonalTabType {
    // 动态,帖子,评论
    TAB_ALL("all"), TAB_FEED("feed"), TAB_COMMENT("comment");

    PersonalTabType(String name) {
        this.name = name;
    }

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
