package com.happen.app.components;

import com.happen.app.util.HappenUser;
import com.parse.ParseObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Spencer on 4/12/14.
 */

public class EventObject implements Serializable{
    public String details;
    public String objectId;
    public ParseObject parseObj;
    public Boolean meToo;

    //used by adapter to represent the case of empty list - still need one item for the list adapter to work so mark it empty
    private boolean empty;

    public EventObject(){
        empty = true;
        meToo = false;
    }

    public EventObject(String deets, String objId)
    {
        this.details = deets;
        this.objectId = objId;
        this.empty = false;
        this.meToo=false;
    }

    public EventObject(String deets, String objId, Boolean m) {
        this(deets,objId);
        meToo = m;
    }

    public EventObject(String deets, String objId, ParseObject parse)
    {
        this(deets, objId);
        this.parseObj = parse;
    }

    public void setEmpty(boolean e)
    {
        this.empty = e;
    }

    public boolean isEmpty()
    {
        return empty;
    }
}
