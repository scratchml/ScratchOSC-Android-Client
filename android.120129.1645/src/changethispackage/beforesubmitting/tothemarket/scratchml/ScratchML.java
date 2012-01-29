package changethispackage.beforesubmitting.tothemarket.scratchml;

import processing.core.*; 

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

import org.slf4j.helpers.*; 
import org.slf4j.*; 
import org.slf4j.spi.*; 
import org.slf4j.impl.*; 
import oscP5.*; 
import netP5.*; 

import android.view.MotionEvent; 
import android.view.KeyEvent; 
import android.graphics.Bitmap; 
import java.io.*; 
import java.util.*; 

public class ScratchML extends PApplet {

/**
 * oscP5parsing by andreas schlegel
 * example shows how to parse incoming osc messages "by hand".
 * it is recommended to take a look at oscP5plug for an
 * alternative and more convenient way to parse messages.
 * oscP5 website at http://www.sojamo.de/oscP5
 */










 



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

float step, points_, frame_R, frame_L, prev_frame_R, prev_frame_L, diff_R, diff_L, progressL, progressR, rotation_L, rotation_R, fade;

public void setup() {
  scLock = new LockScreen(this.getApplicationContext());
 
  sw = screenWidth;
  sh = screenHeight;
  rad = sh/2; 
  // Set this so the sketch won't reset as the phone is rotated:
  orientation(LANDSCAPE);
  // Setup Fonts:
  fontList = PFont.list();
  androidFont = createFont(fontList[0], 16, true);
  textFont(androidFont);

  scLock.Start();
  frameRate(20);
  /* start oscP5, listening for incoming messages at port 12000 */
  oscP5 = new OscP5(this, getLocalIpAddress(), 8319);
  //  
  //  /* myRemoteLocation is a NetAddress. a NetAddress takes 2 parameters,
  //   * an ip address and a port number. myRemoteLocation is used as parameter in
  //   * oscP5.send() when sending osc packets to another computer, device, 
  //   * application. usage see below. for testing purposes the listening port
  //   * and the port of the remote location address are the same, hence you will
  //   * send messages back to this sketch.
  //   */
  myRemoteLocation = new NetAddress(getLocalIpAddress(), 8319);
  smooth();
}

public void draw() {
  float X = sw / 4.0f;
  float Y = sh / 2.0f;
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

  // Draw turntable position


  //frame_R = random(0, 1000);
  //frame_L = random(0, 1000);

  //prev_frame_R = 0;
  //prev_frame_L = 0;
  //diff_R = prev_frame_R - frame_R;
  //diff_R = diff_R*1000;
  //diff_L = prev_frame_L - frame_L;
  println("diff_r :"+abs(diff_R));
  //if (abs(diff_R) > 5900000) { 
  //gNotificationManager.notify(1, gNotification);
  //}
  //if(diff_L < 500){
  //  gNotificationManager.notify(1, gNotification);
  //}

  stroke(50, 50, 250);
  strokeWeight(25);
  frame_L = frame_R;
  rotation_R = map(frame_R, 0, 1800, 0, TWO_PI);
  rotation_L = map(frame_L, 0, 1800, 0, TWO_PI); 
  line(X, Y, cos(rotation_L) * (X*0.92f) + X, sin(rotation_L) * (Y*0.92f) + Y);
  line(3*X, Y, cos(rotation_R) * (X*0.92f) + 3*X, sin(rotation_R) * (Y*0.92f) + Y);
  //prev_frame_R = frame_R;
  //prev_frame_L = frame_L;
}

public void oscEvent(OscMessage theOscMessage) {
  /* check if theOscMessage has the address pattern we are looking for. */
  if (theOscMessage.checkAddrPattern("/scratch/record/right")==true) {
    /* check if the typetag is the right one. */
    //if (theOscMessage.checkTypetag("ifs")) {
    /* parse theOscMessage and extract the values from the osc message arguments. */
    float firstValue = theOscMessage.get(0).floatValue();
    frame_R = firstValue;
    //println("right"+frame_R);
    return;
    //}
  }
  if (theOscMessage.checkAddrPattern("/scratch/record/left")==true) {
    /* check if the typetag is the right one. */
    //if (theOscMessage.checkTypetag("ifs")) {
    /* parse theOscMessage and extract the values from the osc message arguments. */
    float firstValue = theOscMessage.get(0).floatValue();
    frame_L = firstValue;
    //println("left"+frame_L);
    return;
    //}
  }
  if (theOscMessage.checkAddrPattern("/scratch/mixer/fader")==true) {
    /* check if the typetag is the right one. */
    //if (theOscMessage.checkTypetag("ifs")) {
    /* parse theOscMessage and extract the values from the osc message arguments. */
    float firstValue = theOscMessage.get(0).floatValue();
    fade = firstValue;
    return;
    //}
  }
}

public void onResume() {
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


  public int sketchWidth() { return screenWidth; }
  public int sketchHeight() { return screenHeight; }
}
