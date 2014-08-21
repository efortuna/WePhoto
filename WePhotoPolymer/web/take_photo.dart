import 'package:polymer/polymer.dart';
import 'dart:html';

/**
 * A Polymer take-photo element.
 */
@CustomTag('take-photo')
class TakePhoto extends PolymerElement {

  /// Constructor used to create instance of TakePhoto.
  TakePhoto.created() : super.created() {
  }

  /*
   * Optional lifecycle methods - uncomment if needed.
   *

  /// Called when an instance of take-photo is inserted into the DOM.
  attached() {
    super.attached();
  }

  /// Called when an instance of take-photo is removed from the DOM.
  detached() {
    super.detached();
  }

  /// Called when an attribute (such as  a class) of an instance of
  /// take-photo is added, changed, or removed.
  attributeChanged(String name, String oldValue, String newValue) {
  }

  /// Called when take-photo has been fully prepared (Shadow DOM created,
  /// property observers set up, event listeners attached).
  ready() {
  }
   
  */
  
  /// Called when take-photo has been fully prepared (Shadow DOM created, 
  /// property observers set up, event listeners attached).
  ready() {
    ImageElement img = shadowRoot.querySelector('#imageSrc');

    /*window.navigator.getUserMedia(video: true).then((stream) {
        var url = Url.createObjectUrl(stream);
        var video = new VideoElement()
          ..autoplay = true;
        document.body.append(video);
        video.src = url;
        document.query('#stopRtcDemo').onMouseDown.listen(
            (evt) { video.pause(); video.src = ''; stream.stop();});
      });*/
    
    
    
    
    VideoElement video = shadowRoot.querySelector('video');
    CanvasElement canvas = shadowRoot.querySelector('#takenPhoto');
    var ctx = canvas.getContext('2d');
    var localMediaStream = null;
    
    video.onClick.listen((MouseEvent event) {
      print('on click listened');
      if (localMediaStream) {
        ctx.drawImage(video, 0, 0);
        // "image/webp" works in Chrome.
        // Other browsers will fall back to image/png.
        (shadowRoot.querySelector('#imageSrc') as ImageElement).src = canvas.toDataUrl('image/webp');
      }
    });

    // Not showing vendor prefixes or code that works cross-browser.
    window.navigator.getUserMedia(video: true).then((aStream) {
      video.src = Url.createObjectUrl(aStream); 
      localMediaStream = aStream;
    }).catchError((error) => print('there was an error $error')); 
  }
  
}
