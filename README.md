ScreenCastDemo
==============

This is a FFmpeg 4.0 android library demo built over android MediaCodec Encoders(H.264, AAC).<br>
You - as a developer - can easily cast android screen with this library to Network (RTSP, RTMP, UDP, ...) including File.

## The APIs you have to know : 

### prepare an instance
* mMC264Recorder = new MC264ScreenRecorder();
* mMC264Recorder.registerCallback( new MyMC264RecorderCallback() );
* mMC264Recorder.init( this );
<br>
* mMC264Recorder.onActivityResult(requestCode, resultCode, data);
<br>
* mMC264Recorder.setCaptureSize( mDisplayWidth, mDisplayHeight );
* mMC264Recorder.setDst(capDstStr);
* mMC264Recorder.setLandscapeMode(true/false);

### start
* mMC264Recorder.start();

### stop
* mMC264Recorder.start();

### release
* mMC264Recorder.release();

## Limitations :
* The running time is limited to 30 minutes. Contact me for the unlimited version.



## Screenshot
<p align="center">
  <img src="./ScreenCastDemo-Portrait.png" width="350" height="720">
</p>

* App on google play : https://play.google.com/store/apps/details?id=com.iocaster.ffmpegmc264demoapp
<br>
* Othre Demo App on google play : https://play.google.com/store/apps/details?id=com.iocaster.ffmpegmc264demoapp

<br>
<br>




