package com.bumbumapps.hbrecorder;

public interface HBRecorderListener {
    void HBRecorderOnStart();
    void HBRecorderOnComplete();
    void HBRecorderOnError(int errorCode, String reason);
    void backtohome();
    void backtomyrecording();
}
