package com.cels.photofun;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TableRow;


class TableRowExtended extends TableRow {


    public TableRowExtended(Context context) {
        super(context);
    }

    public TableRowExtended(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    void disableChildren() {
        for (int i = 0; i < this.getChildCount(); i++) {
            View view = this.getChildAt(i);
            view.setEnabled(false);
        }
    }

    void enableChildren() {
        for (int i = 0; i < this.getChildCount(); i++) {
            View view = this.getChildAt(i);
            view.setEnabled(true);
        }
    }

}