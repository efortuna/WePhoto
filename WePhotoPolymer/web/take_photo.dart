import 'package:polymer/polymer.dart';
import 'dart:html';
import 'google_drive.dart';

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
<<<<<<< HEAD

=======
>>>>>>> can upload photos
   
  */

  /// Called when take-photo has been fully prepared (Shadow DOM created,
  /// property observers set up, event listeners attached).
  ready() {
    // TO use the forms based camera, uncomment this, and comment
    // out the rest of this method (to avoid the cameras conflicting)
    // Also uncomment the appropriate html in take_photo.html
    //    var imageCapture = this.$['capture'];
    //    imageCapture.onChange.listen(handleImage);

     VideoElement video = shadowRoot.querySelector('video');
     CanvasElement canvas = shadowRoot.querySelector('#takenPhoto');
     ButtonElement button = shadowRoot.querySelector('#accept');
     var ctx = canvas.getContext('2d');
     var localMediaStream = null;
     var takePhoto = true;
         
     button.onClick.listen((MouseEvent event) {
       if (localMediaStream != null && takePhoto) {
         canvas.width = 640;
         canvas.height = 480;
         ctx.drawImageScaled(video, 0, 0, 640, 480);
         video.pause();
         button.innerHtml = 'Upload';
         takePhoto = !takePhoto;
       } else if (!takePhoto) {
         // TODO: send the picture in "ctx" off to Google Docs!
         new GoogleDrive().uploadDrive.then((drive) {
           var params = {};
           drive.upload('files', 'POST',  
               '{"title": "${new DateTime.now().millisecondsSinceEpoch}"}',
               canvas.toDataUrl().split(',')[1],
               'multipart/related;', queryParams: params).then((response) {
             print(response);
           });
         });
         
         video.play();
         button.innerHtml = 'Snap Photo';
         takePhoto = !takePhoto;
       }
     });

     window.navigator.getUserMedia(video: true).then((aStream) {
       video.src = Url.createObjectUrl(aStream); 
       localMediaStream = aStream;
     }).catchError((error) => print('there was an error $error')); 
  }

  void handleImage(Event e) {
    CanvasElement canvas = shadowRoot.querySelector('#takenPhoto');
    var ctx = canvas.getContext('2d');
    InputElement captureInput = this.$['capture'];
    FileList files = captureInput.files;
    File file = files.item(0);
    FileReader reader = new FileReader();
    reader.readAsDataUrl( file );
    reader.onLoadEnd.listen((Event e) {
        ImageElement display = this.$['displayimage'];
        display.src = reader.result;
    });
  }

}
