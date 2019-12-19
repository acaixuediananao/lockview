package com.wxl.lockview;

public interface OnCreateGesturePasswordListener {

    void onFirstCreateSuccess(String password);
    void onSecondCreateSuccess(String password);
    void onCreateFailed(int resonType);
}
