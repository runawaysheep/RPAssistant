package com.rAs.android.rpgamepad;

public class InputInfo {
	private int keyCode;
	private int output;
	private boolean isAxis;
	private boolean isWithCombine;
	private boolean isNegative;
	private String toProfile;
	
	public int getKeyCode() {
		return keyCode;
	}
	public void setKeyCode(int keyCode) {
		this.keyCode = keyCode;
	}
	public int getOutput() {
		return output;
	}
	public void setOutput(int output) {
		this.output = output;
	}
	public boolean isAxis() {
		return isAxis;
	}
	public void setAxis(boolean isAxis) {
		this.isAxis = isAxis;
	}
	public boolean isWithCombine() {
		return isWithCombine;
	}
	public void setWithCombine(boolean isWithCombine) {
		this.isWithCombine = isWithCombine;
	}
	public boolean isNegative() {
		return isNegative;
	}
	public void setNegative(boolean isNegative) {
		this.isNegative = isNegative;
	}
	public String getToProfile() {
		return toProfile;
	}
	public void setToProfile(String toProfile) {
		this.toProfile = toProfile;
	}
}
