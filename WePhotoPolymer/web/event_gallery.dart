import 'package:polymer/polymer.dart';
import 'google_drive.dart';

/**
 * A Polymer event-gallery element.
 */
@CustomTag('event-gallery')
class EventGallery extends PolymerElement {
  @published List<String> photos = [];

  /// Constructor used to create instance of EventGallery.
  EventGallery.created() : super.created() {
    refreshImages();
  }
  
  ready() {
    this.$['refresh'].on['click'].listen((_) {
      print('here');
      refreshImages();
    });
  }
  
  refreshImages() {
    photos = [];
    new GoogleDrive().drive.then((drive) {
      var params = {
          'q': 'trashed = false and not mimeType contains "folder"'
      };
      drive.request('files', 'GET', queryParams: params).then((response) {
        photos = response['items'].map((item) => item['thumbnailLink']).toList();
      });
    });
  }
}
