package com.happen.app.components;

import com.happen.app.util.HappenUser;
import com.parse.ParseObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Spencer on 4/12/14.
 */

public class EventObject implements Serializable{
    public String owner;
    public String details;
    public String timeFrame;
    public String objectId;
    public ParseObject parseObj;
    public Boolean meToo;

    public Boolean empty;

    public EventObject(){
        meToo = false;
        empty = true;
    }

    public EventObject(String details, String objId, ParseObject parseObj)
    {
        this.details = details;
        this.objectId = objId;
        this.parseObj = parseObj;
        this.meToo = false;
        this.empty = false;
    }

    public EventObject(String name, String details, String time, String objId)
    {
        this.owner = name;
        this.details = details;
        this.timeFrame = time;
        this.objectId = objId;
        this.meToo = false;
        this.empty = false;

    }

    public EventObject(String name, String details, String time, String objId, Boolean m) {
        this(name, details, time, objId);
        meToo = m;
    }

    public EventObject(String name, String details, String time, String objId, ParseObject parse)
    {
        this(name, details, time, objId);
        this.parseObj = parse;
    }

    public Boolean isEmpty(){
        return empty;
    }

    public void setEmpty(Boolean empty){
        this.empty = empty;
    }

}
