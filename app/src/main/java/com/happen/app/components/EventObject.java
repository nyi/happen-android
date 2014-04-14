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
    public ArrayList<HappenUser> meToos;
    public String objectId;
    public ParseObject parseObj;

    //used by adapter to represent the case of empty list - still need one item for the list adapter to work so mark it empty
    private boolean empty;

    public EventObject(){
        empty = true;
    }

    public EventObject(String deets, String objId)
    {
        this.details = deets;
        this.objectId = objId;
        this.empty = false;
    }

    public EventObject(String deets, String objId, ParseObject parse)
    {
        this.details = deets;
        this.objectId = objId;
        this.empty = false;
        this.parseObj = parse;
    }

    public EventObject(String deets, String objId, ArrayList<HappenUser> meTooed)
    {
        this(deets, objId);
        this.meToos = meTooed;
    }

    public EventObject(String deets, String objId, ArrayList<HappenUser> meTooed, ParseObject parse)
    {
        this(deets, objId, meTooed);
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
