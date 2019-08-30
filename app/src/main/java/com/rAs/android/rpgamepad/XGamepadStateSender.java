package com.rAs.android.rpgamepad;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class XGamepadStateSender {
//	private static Field fAccelerationX;
//	private static Field fAccelerationY;
//	private static Field fAccelerationZ;
//	private static Field fAnalogButtons;
//	private static Field fAngularVelocityX;
//	private static Field fAngularVelocityY;
//	private static Field fAngularVelocityZ;
	private static Field fButtonState;
	private static Field fChangedButtons;
//	private static Field fConnected;
	private static Field fLeftAxisX;
	private static Field fLeftAxisY;
	private static Field fLeftTrigger;
//	private static Field fOrientationW;
//	private static Field fOrientationX;
//	private static Field fOrientationY;
//	private static Field fOrientationZ;
//	private static Field fPlayerID;
	private static Field fRightAxisX;
	private static Field fRightAxisY;
	private static Field fRightTrigger;
	
	private static Object gamepadState;
	
    private static Method gamepadStateChangedMethod;
    private static Method sensorStateChangedMethod;
	
	public static void init(Class<?> gamepadStateClass, Class<?> gamepadCallback) {
		try {
			gamepadState = gamepadStateClass.newInstance();
			
//			fAccelerationX 		=	 gamepadStateClass.getDeclaredField("mAccelerationX");			fAccelerationX.setAccessible(true);
//			fAccelerationY 		=	 gamepadStateClass.getDeclaredField("mAccelerationY");			fAccelerationY.setAccessible(true);
//			fAccelerationZ 		=	 gamepadStateClass.getDeclaredField("mAccelerationZ");			fAccelerationZ.setAccessible(true);
//			fAnalogButtons 		=	 gamepadStateClass.getDeclaredField("mAnalogButtons");			fAnalogButtons.setAccessible(true);
//			fAngularVelocityX 	=	 gamepadStateClass.getDeclaredField("mAngularVelocityX");		fAngularVelocityX.setAccessible(true);
//			fAngularVelocityY 	=	 gamepadStateClass.getDeclaredField("mAngularVelocityY");		fAngularVelocityY.setAccessible(true);
//			fAngularVelocityZ 	=	 gamepadStateClass.getDeclaredField("mAngularVelocityZ");		fAngularVelocityZ.setAccessible(true);
			fButtonState 		=	 gamepadStateClass.getDeclaredField("mButtonState");			fButtonState.setAccessible(true);
			fChangedButtons 	=	 gamepadStateClass.getDeclaredField("mChangedButtons");			fChangedButtons.setAccessible(true);
//			fConnected 			=	 gamepadStateClass.getDeclaredField("mConnected");				fConnected.setAccessible(true);
			fLeftAxisX 			=	 gamepadStateClass.getDeclaredField("mLeftAxisX");				fLeftAxisX.setAccessible(true);
			fLeftAxisY 			=	 gamepadStateClass.getDeclaredField("mLeftAxisY");				fLeftAxisY.setAccessible(true);
			fLeftTrigger 		=	 gamepadStateClass.getDeclaredField("mLeftTrigger");			fLeftTrigger.setAccessible(true);
//			fOrientationW 		=	 gamepadStateClass.getDeclaredField("mOrientationW");			fOrientationW.setAccessible(true);
//			fOrientationX 		=	 gamepadStateClass.getDeclaredField("mOrientationX");			fOrientationX.setAccessible(true);
//			fOrientationY 		=	 gamepadStateClass.getDeclaredField("mOrientationY");			fOrientationY.setAccessible(true);
//			fOrientationZ 		=	 gamepadStateClass.getDeclaredField("mOrientationZ");			fOrientationZ.setAccessible(true);
//			fPlayerID 			=	 gamepadStateClass.getDeclaredField("mPlayerID");				fPlayerID.setAccessible(true);
			fRightAxisX 		=	 gamepadStateClass.getDeclaredField("mRightAxisX");				fRightAxisX.setAccessible(true);
			fRightAxisY 		=	 gamepadStateClass.getDeclaredField("mRightAxisY");				fRightAxisY.setAccessible(true);
			fRightTrigger 		=	 gamepadStateClass.getDeclaredField("mRightTrigger");			fRightTrigger.setAccessible(true);

			gamepadStateChangedMethod = gamepadCallback.getMethod("gamepadStateChanged", int.class, gamepadStateClass);
			sensorStateChangedMethod = gamepadCallback.getMethod("sensorStateChanged", int.class, gamepadStateClass);
			
		} catch (Exception e) {
			XRPAssistant.log(e);
		}
	}
	

	public static void applyGamepadState(boolean sensor, PSGamepadHandler handler) {
		PSGamepadValues gamepadValues = handler.getGamepadValues();
		try {
			fButtonState.set(gamepadState, gamepadValues.getButtonState());
			fChangedButtons.set(gamepadState, gamepadValues.getChangedButtons());
			
			if(sensor) {
				//fAccelerationX.set(gamepadState, gamepadValues.getAccelerationX());
				//fAccelerationY.set(gamepadState, gamepadValues.getAccelerationY());
				//fAccelerationZ.set(gamepadState, gamepadValues.getAccelerationZ());
				//fAnalogButtons.set(gamepadState, gamepadValues.getAnalogButtons());
				//fAngularVelocityX.set(gamepadState, gamepadValues.getAngularVelocityX());
				//fAngularVelocityY.set(gamepadState, gamepadValues.getAngularVelocityY());
				//fAngularVelocityZ.set(gamepadState, gamepadValues.getAngularVelocityZ());
				//fConnected.set(gamepadState, gamepadValues.getConnected());
				fLeftAxisX.set(gamepadState, gamepadValues.getLeftAxisX());
				fLeftAxisY.set(gamepadState, gamepadValues.getLeftAxisY());
				fLeftTrigger.set(gamepadState, gamepadValues.getLeftTrigger());
				//fOrientationW.set(gamepadState, gamepadValues.getOrientationW());
				//fOrientationX.set(gamepadState, gamepadValues.getOrientationX());
				//fOrientationY.set(gamepadState, gamepadValues.getOrientationY());
				//fOrientationZ.set(gamepadState, gamepadValues.getOrientationZ());
				//fPlayerID.set(gamepadState, gamepadValues.getPlayerID());
				fRightAxisX.set(gamepadState, gamepadValues.getRightAxisX());
				fRightAxisY.set(gamepadState, gamepadValues.getRightAxisY());
				fRightTrigger.set(gamepadState, gamepadValues.getRightTrigger());
			}

			runGamepadStateChanged();
			
			if(sensor)
				runSensorStateChanged();

			
		} catch (Exception e) {
			XRPAssistant.log(e);
		}
	}
	
	

	private static void runGamepadStateChanged() {
		try {
			gamepadStateChangedMethod.invoke(null, 0, gamepadState);
		} catch (Exception e) {
			XRPAssistant.log(e);
		}
	}
	
	private static void runSensorStateChanged() {
		try {
			sensorStateChangedMethod.invoke(null, 0, gamepadState);
		} catch (Exception e) {
			XRPAssistant.log(e);
		}
	}
}
