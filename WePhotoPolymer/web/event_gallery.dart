import 'dart:html';
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
    new GoogleDrive().drive.then((drive) {
      var params = {
          'access_token': drive.auth.token.data,
          'q': 'trashed = false and not mimeType contains "folder"'
      };
      drive.request('files', 'GET', queryParams: params).then((response) {
        photos = response['items'].map((item) => item['thumbnailLink']);
      });
    });
  }
  
}
