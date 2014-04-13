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

    public NewsObject(String type, String nameTarget, String nameSource)
    {
        this.type = type;
        this.nameSource = nameSource;
        this.nameTarget = nameTarget;
    }

    public NewsObject(String type, String nameTarget, String nameSource, String event)
    {
        this(type, nameTarget, nameSource);
        this.event = event;
    }

    public String toString()
    {
        return "NewsObj: " + type + "- " + nameSource;
    }
}