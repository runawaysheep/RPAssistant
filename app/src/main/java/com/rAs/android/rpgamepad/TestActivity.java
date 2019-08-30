package com.rAs.android.rpgamepad;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class TestActivity extends Activity implements PSGamepadHandler.OnGamepadStateChangeListener {
    private ImageView movedLeftAnalog, movedRightAnalog,
            pressedCircle, pressedCross, pressedDdown, pressedDleft, pressedDright, pressedDup,
            pressedL1, pressedL2, pressedL3,
            pressedOption, pressedPs,
            pressedR1, pressedR2, pressedR3,
            pressedShare, pressedSquare, pressedTouch, pressedTriangle;
    private Point leftAnalogCenter, rightAnalogCenter;
    private float scale;
    private PSGamepadHandler psGamepadHandler;
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_gamepad);

        scale = getResources().getDisplayMetrics().density;

        psGamepadHandler = new PSGamepadHandler(this, null, PreferenceManager.getDefaultSharedPreferences(this));
        psGamepadHandler.setOnGamepadStateChangeListener(this);

        final View contentView = findViewById(android.R.id.content);

        movedLeftAnalog = findViewById(R.id.imageView_moved_left_analog);
        movedRightAnalog = findViewById(R.id.imageView_moved_right_analog);
        pressedCircle = findViewById(R.id.imageView_pressed_circle);
        pressedCross = findViewById(R.id.imageView_pressed_cross);
        pressedDdown = findViewById(R.id.imageView_pressed_ddown);
        pressedDleft = findViewById(R.id.imageView_pressed_dleft);
        pressedDright = findViewById(R.id.imageView_pressed_dright);
        pressedDup = findViewById(R.id.imageView_pressed_dup);
        pressedL1 = findViewById(R.id.imageView_pressed_l1);
        pressedL2 = findViewById(R.id.imageView_pressed_l2);
        pressedL3 = findViewById(R.id.imageView_pressed_l3);
        pressedOption = findViewById(R.id.imageView_pressed_option);
        pressedPs = findViewById(R.id.imageView_pressed_ps);
        pressedR1 = findViewById(R.id.imageView_pressed_r1);
        pressedR2 = findViewById(R.id.imageView_pressed_r2);
        pressedR3 = findViewById(R.id.imageView_pressed_r3);
        pressedShare = findViewById(R.id.imageView_pressed_share);
        pressedSquare = findViewById(R.id.imageView_pressed_square);
        pressedTouch = findViewById(R.id.imageView_pressed_touch);
        pressedTriangle = findViewById(R.id.imageView_pressed_triangle);

        movedLeftAnalog.setVisibility(View.INVISIBLE);
        movedRightAnalog.setVisibility(View.INVISIBLE);

        onGamepadStateChange(true);

        contentView.post(new Runnable(){
            @Override
            public void run() {
                final RelativeLayout gamepadLayout = findViewById(R.id.relativeLayout_gamepad);
                final int contentViewWidth = contentView.getMeasuredWidth();
                final int contentViewHeight = contentView.getMeasuredHeight();
                gamepadLayout.post(new Runnable(){
                    @Override
                    public void run() {
                        float layoutScale = 0.9f * contentViewWidth / gamepadLayout.getMeasuredWidth();
                        float layoutScaleY = 0.9f * contentViewHeight / gamepadLayout.getMeasuredHeight();

                        if(layoutScale > layoutScaleY) layoutScale = layoutScaleY;

                        gamepadLayout.setScaleX(layoutScale);
                        gamepadLayout.setScaleY(layoutScale);

                    }
                });
            }
        });
        movedLeftAnalog.post(new Runnable() {
            @Override
            public void run() {
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)movedLeftAnalog.getLayoutParams();
                leftAnalogCenter = new Point(lp.leftMargin, lp.topMargin);
            }
        });
        movedRightAnalog.post(new Runnable() {
            @Override
            public void run() {
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)movedRightAnalog.getLayoutParams();
                rightAnalogCenter = new Point(lp.leftMargin , lp.topMargin );
            }
        });
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch(event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
                return super.dispatchKeyEvent(event);
            case KeyEvent.KEYCODE_UNKNOWN:
                showToast(getText(R.string.msg_change_english_input));
                return true;
            default:
                psGamepadHandler.setGamepadKeyState(event);
                return true;
        }
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent ev) {
        psGamepadHandler.setGamepadAxisState(ev);
        return super.dispatchGenericMotionEvent(ev);
    }

    @Override
    public void onGamepadStateChange(boolean sensor) {
        PSGamepadValues gamepadValues = psGamepadHandler.getGamepadValues();
        if(sensor) {
            pressedL2.setAlpha(1f * gamepadValues.getLeftTrigger() / PSGamepadHandler.TRIGGER_MAX_VALUE);
            pressedR2.setAlpha(1f * gamepadValues.getRightTrigger() / PSGamepadHandler.TRIGGER_MAX_VALUE);

            if(leftAnalogCenter != null) {
                int left = Integer.MIN_VALUE, top = Integer.MIN_VALUE;
                if(Math.abs(gamepadValues.getLeftAxisX()) > PSGamepadHandler.ANALOG_STICK_MAX_VALUE * psGamepadHandler.getAnalogDeadZone())
                    left = (int) (13f * gamepadValues.getLeftAxisX() / PSGamepadHandler.ANALOG_STICK_MAX_VALUE * scale);
                if(Math.abs(gamepadValues.getLeftAxisY()) > PSGamepadHandler.ANALOG_STICK_MAX_VALUE * psGamepadHandler.getAnalogDeadZone())
                    top = (int) (-13f * gamepadValues.getLeftAxisY() / PSGamepadHandler.ANALOG_STICK_MAX_VALUE * scale);

                if(left != Integer.MIN_VALUE || top != Integer.MIN_VALUE) {
                    if(left == Integer.MIN_VALUE) left = 0;
                    if(top == Integer.MIN_VALUE) top = 0;
                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) movedLeftAnalog.getLayoutParams();
                    lp.setMargins(leftAnalogCenter.x + left, leftAnalogCenter.y + top, 0, 0);
                    movedLeftAnalog.setLayoutParams(lp);
                    movedLeftAnalog.setVisibility(View.VISIBLE);
                } else {
                    movedLeftAnalog.setVisibility(View.INVISIBLE);
                }
            }

            if(rightAnalogCenter != null) {
                int left = Integer.MIN_VALUE, top = Integer.MIN_VALUE;
                if(Math.abs(gamepadValues.getRightAxisX()) > PSGamepadHandler.ANALOG_STICK_MAX_VALUE * psGamepadHandler.getAnalogDeadZone())
                    left = (int) (13f * gamepadValues.getRightAxisX() / PSGamepadHandler.ANALOG_STICK_MAX_VALUE * scale);
                if(Math.abs(gamepadValues.getRightAxisY()) > PSGamepadHandler.ANALOG_STICK_MAX_VALUE * psGamepadHandler.getAnalogDeadZone())
                    top = (int) (-13f * gamepadValues.getRightAxisY() / PSGamepadHandler.ANALOG_STICK_MAX_VALUE * scale);

                if(left != Integer.MIN_VALUE || top != Integer.MIN_VALUE) {
                    if(left == Integer.MIN_VALUE) left = 0;
                    if(top == Integer.MIN_VALUE) top = 0;
                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) movedRightAnalog.getLayoutParams();
                    lp.setMargins(rightAnalogCenter.x + left, rightAnalogCenter.y + top, 0, 0);
                    movedRightAnalog.setLayoutParams(lp);
                    movedRightAnalog.setVisibility(View.VISIBLE);
                } else {
                    movedRightAnalog.setVisibility(View.INVISIBLE);
                }
            }
        }

        setButtonPressed(PSGamepadHandler.Gamepad.DPAD_UP, pressedDup);
        setButtonPressed(PSGamepadHandler.Gamepad.DPAD_RIGHT, pressedDright);
        setButtonPressed(PSGamepadHandler.Gamepad.DPAD_DOWN, pressedDdown);
        setButtonPressed(PSGamepadHandler.Gamepad.DPAD_LEFT, pressedDleft);

        setButtonPressed(PSGamepadHandler.Gamepad.BUTTON_B, pressedCircle);
        setButtonPressed(PSGamepadHandler.Gamepad.BUTTON_A, pressedCross);
        setButtonPressed(PSGamepadHandler.Gamepad.BUTTON_Y, pressedTriangle);
        setButtonPressed(PSGamepadHandler.Gamepad.BUTTON_X, pressedSquare);

        setButtonPressed(PSGamepadHandler.Gamepad.BUTTON_LEFT_SHOULDER, pressedL1);
        setButtonPressed(PSGamepadHandler.Gamepad.BUTTON_LEFT_THUMB, pressedL3);

        setButtonPressed(PSGamepadHandler.Gamepad.BUTTON_RIGHT_SHOULDER, pressedR1);
        setButtonPressed(PSGamepadHandler.Gamepad.BUTTON_RIGHT_THUMB, pressedR3);

        setButtonPressed(PSGamepadHandler.Gamepad.BUTTON_START, pressedOption);
        setButtonPressed(PSGamepadHandler.Gamepad.BUTTON_BACK, pressedShare);
        setButtonPressed(PSGamepadHandler.Gamepad.BUTTON_TOUCHPAD, pressedTouch);
        setButtonPressed(PSGamepadHandler.Gamepad.BUTTON_PS, pressedPs);
    }


    private void setButtonPressed(int button, ImageView iv) {
        iv.setVisibility((psGamepadHandler.getGamepadValues().getButtonState() & button) == button ? View.VISIBLE : View.INVISIBLE);
    }

    private void showToast(CharSequence msg) {
        if(toast != null)
            toast.cancel();
        toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        toast.show();
    }
}
