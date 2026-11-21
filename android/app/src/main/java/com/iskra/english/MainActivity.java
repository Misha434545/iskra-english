package com.iskra.english;

import android.os.Bundle;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(IskraBridge.class);
        super.onCreate(savedInstanceState);
    }
}
