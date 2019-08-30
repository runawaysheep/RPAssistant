package com.rAs.android.rpgamepad;

import android.app.AlertDialog;
import android.content.Context;
import android.view.MotionEvent;

public class InputDialog extends AlertDialog {

	interface OnMotionEventListener {
		boolean onGenericMotionEvent(MotionEvent event);
	}
	
	private OnMotionEventListener onMotionEventListener;
	private Context mContext;
	
	public InputDialog(Context context, int theme) {
		super(context, theme);
		mContext = context;
	}

	public InputDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		mContext = context;
	}

	public InputDialog(Context context) {
		super(context);
		mContext = context;
	}

	public void setOnInputListener(OnMotionEventListener onInputListener) {
		this.onMotionEventListener = onInputListener;
	}

	@Override
	public boolean dispatchGenericMotionEvent(MotionEvent event) {
		if(onMotionEventListener != null)
			return onMotionEventListener.onGenericMotionEvent(event);
		return super.dispatchGenericMotionEvent(event);
	}
	
    public void setPositiveButton(int textId, final OnClickListener listener) {
    	setButton(BUTTON_POSITIVE, mContext.getText(textId), listener);
    }
    
    public void setNegativeButton(int textId, final OnClickListener listener) {
    	setButton(BUTTON_NEGATIVE, mContext.getText(textId), listener);
    }
    
    public void setNeutralButton(int textId, final OnClickListener listener) {
    	setButton(BUTTON_NEUTRAL, mContext.getText(textId), listener);
    }
}
