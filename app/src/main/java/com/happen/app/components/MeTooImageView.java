package com.happen.app.components;

import android.app.ActionBar;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.happen.app.util.Util;
import com.happen.app.util.FlowLayout;

/**
 * Created by Spencer on 4/13/14.
 */
public class MeTooImageView extends ImageView{

    private int dpdimension = 50;


    public MeTooImageView(Context context){
        super(context);
        this.setAdjustViewBounds(true);
        this.setMaxHeight((int) Util.dipToPixels(context, dpdimension));
        this.setMaxWidth((int) Util.dipToPixels(context, dpdimension));

    }
}
