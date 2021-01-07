ScreenCastDemo
==============

This is a FFmpeg 4.0 android library demo built over android MediaCodec Encoders(H.264, AAC).<br>
You - as a developer - can easily cast android screen with this library to Network (RTSP, RTMP, UDP, ...) including File.

## The APIs you have to know : 

### prepare an instance
* mMC264Recorder = new MC264ScreenRecorder();
* mMC264Recorder.registerCallback( new MyMC264RecorderCallback() );
* mMC264Recorder.init( this );
* mMC264Recorder.onActivityResult(requestCode, resultCode, data); -- for targetSdkVersion 28 or below
* mMC264Recorder.startProjection(int resultCode, Intent data);    -- for targetSdkVersion 29 or above within foreground service
* mMC264Recorder.setCaptureSize( mDisplayWidth, mDisplayHeight );
* mMC264Recorder.setDst(capDstStr);
* mMC264Recorder.setLandscapeMode(true/false);

### start
* mMC264Recorder.start();

### stop
* mMC264Recorder.stop();

### release
* mMC264Recorder.release();


## Screenshot
<p align="center">
  <img src="./ScreenCastDemo-Portrait.png" width="350" height="720">
</p>

* This App on google play : https://play.google.com/store/apps/details?id=com.iocaster.screencastdemoapp
<br>
* Othre Demo App on google play : https://play.google.com/store/apps/details?id=com.iocaster.ffmpegmc264demoapp


## Class Diagram
<p align="center">
  <img src="./MC264ScreenRecorder.jpg">
</p>


<br>
<br>




