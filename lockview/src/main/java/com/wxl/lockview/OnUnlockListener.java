package com.wxl.lockview;

public interface OnUnlockListener {

    void onUnlock(String password);
    void onError(int reasonType);
}
