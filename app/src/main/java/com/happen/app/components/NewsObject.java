package com.happen.app.components;

/**
 * Created by Spencer on 4/12/14.
 */
public class NewsObject
{
    public String type;
    public String nameTarget;
    public String nameSource;
    public String event;
    public boolean isUnread;

    public NewsObject(String type, String nameTarget, String nameSource, boolean isUnread)
    {
        this.type = type;
        this.nameSource = nameSource;
        this.nameTarget = nameTarget;
        this.isUnread = isUnread;
    }

    public NewsObject(String type, String nameTarget, String nameSource, String event, boolean isUnread)
    {
        this(type, nameTarget, nameSource, isUnread);
        this.event = event;

    }

    public String toString()
    {
        return "NewsObj: " + type + "- " + nameSource;
    }
}