package com.luakit.flutterluakitdemo;

import android.os.Bundle;

import com.common.luakit.LuaHelper;

import io.flutter.app.FlutterActivity;
import io.flutter.plugins.GeneratedPluginRegistrant;


public class MainActivity extends FlutterActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    LuaHelper.startLuaKit(this);
    GeneratedPluginRegistrant.registerWith(this);
  }
}
