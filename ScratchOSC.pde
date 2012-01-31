/**
* ScratchOSC Android Client
* Renders Realtime ScratchOSC Data via OSC from a Turntable running ScratchOSC
* This was developed as Part of the ArtHackDay at 319 Scholes, Brooklyn, NY
* January 26-28, 2012
* Written By: Daniel Moore
*/

//Copyright (C) 2012 Daniel Moore
//
//Permission is hereby granted, free of charge, to any person obtaining a copy of
//this software and associated documentation files (the "Software"), to deal in
//the Software without restriction, including without limitation the rights to
//use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
//of the Software, and to permit persons to whom the Software is furnished to do
//so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in all
//copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
//SOFTWARE.

import android.app.Notification;
import android.app.NotificationManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import java.net.*; 
import oscP5.*;
import netP5.*;

// Setup vibration globals:
NotificationManager gNotificationManager;
Notification gNotification;
long[] gVibrate = {
  0, 250, 50, 125, 50, 62
};


//-----------------------------------------------------------------------------------------
// Screen Data:
float sw, sh;
// Font Data:
String[] fontList;
PFont androidFont;

// Setup variables for the SensorManager, the SensorEventListeners,
// the Sensors, and the arrays to hold the resultant sensor values:

LockScreen scLock;
OscP5 oscP5;
NetAddress myRemoteLocation;

float rad;

float step, frame_R, frame_L, progressL, progressR, rotation_L, rotation_R, fade;

void setup() {
  scLock = new LockScreen(this.getApplicationContext());
  size(screenWidth, screenHeight);
  sw = screenWidth;
  sh = screenHeight;
  rad = sh/2; 
  // Set this so the sketch won't reset as the phone is rotated:
  orientation(LANDSCAPE);
  fade = 1.0;
  scLock.Start();
  frameRate(20);
  
  /* starts oscP5, listening for incoming messages at port 12000 
   * this should automatically grab the IP of your device 
   * please note you need to be connected to WiFi 
   */
  oscP5 = new OscP5(this, getLocalIpAddress(), 8319);
    
    /* myRemoteLocation is a NetAddress. a NetAddress takes 2 parameters,
     * an ip address and a port number. myRemoteLocation is used as parameter in
     * oscP5.send() when sending osc packets to another computer, device, 
     * application. usage see below. for testing purposes the listening port
     * and the port of the remote location address are the same, hence you will
     * send messages back to this sketch.
     */
  myRemoteLocation = new NetAddress(getLocalIpAddress(), 8319);
  smooth();
}

void draw() {
  float X = sw / 4.0;
  float Y = sh / 2.0;
  step = 100;

  // Fill canvas grey
  background(255);

  // Draw L circle
  fill(230, 230, 230);
  noStroke();
  ellipse( X, Y, rad, rad );

  // Draw R circle
  fill(230, 230, 230);
  noStroke();
  ellipse( 3*X, Y, rad, rad );

  progressL = (frameCount*step)/300000;
  stroke(220);
  strokeWeight(20);
  arc(X, Y, 350, 350, PI+HALF_PI, 4*PI);
  stroke(180);
  arc(X, Y, 350, 350, PI+HALF_PI, (2*PI*progressL) + PI+HALF_PI);

  progressR = (frameCount*step)/300000; //
  stroke(220);
  strokeWeight(20);
  arc(3*X, Y, 350, 350, PI+HALF_PI, 4*PI);
  stroke(180);
  arc(3*X, Y, 350, 350, PI+HALF_PI, (2*PI*progressR) + PI+HALF_PI);

  
  /*turns on the device vibrator if the fader it hit */
  
  if(fade == 0.0){
    gNotificationManager.notify(1, gNotification);
    fade = 1.0;
  }
  

  stroke(50, 50, 250);
  strokeWeight(25);
  frame_L = frame_R;
  rotation_R = map(frame_R, 0, 1800, 0, TWO_PI);
  rotation_L = map(frame_L, 0, 1800, 0, TWO_PI); 
  line(X, Y, cos(rotation_L) * (X*0.92) + X, sin(rotation_L) * (Y*0.92) + Y);
  line(3*X, Y, cos(rotation_R) * (X*0.92) + 3*X, sin(rotation_R) * (Y*0.92) + Y);

}

/*
*  ScratchML OSC Message Format 
*   
*  /scratch/record/right - Right Turntable float pos
*  /scratch/record/left  - Left Turntable  float pos
*  /scratch/mixer/fader  - Crossfader      int  0 = left
*                                               1 = right
*
*/

void oscEvent(OscMessage theOscMessage) {
  
  if (theOscMessage.checkAddrPattern("/scratch/record/right")==true) {
    float firstValue = theOscMessage.get(0).floatValue();
    frame_R = firstValue;
    return;
  }
  if (theOscMessage.checkAddrPattern("/scratch/record/left")==true) {
    float firstValue = theOscMessage.get(0).floatValue();
    frame_L = firstValue;
    //println("left"+frame_L);
    return;
  }
  if (theOscMessage.checkAddrPattern("/scratch/mixer/fader")==true) {
    float firstValue = theOscMessage.get(0).floatValue();
    fade = firstValue;
    return;
  }
}

void onResume() {
  super.onResume();
  gNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
  // Create our Notification that will do the vibration:
  gNotification = new Notification();
  // Set the vibration:
  gNotification.vibrate = gVibrate;
}


public String getLocalIpAddress() {
  try {
    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
      NetworkInterface intf = en.nextElement();
      for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
        InetAddress inetAddress = enumIpAddr.nextElement();
        if (!inetAddress.isLoopbackAddress()) {
          return inetAddress.getHostAddress().toString();
        }
      }
    }
  } 
  catch (SocketException ex) {
    // Log.e(LOG_TAG, ex.toString());
  }
  return null;
}

class LockScreen {
  PowerManager pm;
  Context context;
  WakeLock wl;

  public LockScreen(Context parent) {
    this.context = parent;
    pm =(PowerManager) parent.getSystemService(Context.POWER_SERVICE);
    wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
  }

  public void Start() {
    wl.acquire();
  }

  public void Stop() {
    wl.release();
  }
}

