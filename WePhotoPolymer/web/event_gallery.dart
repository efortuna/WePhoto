library WePhoto.EventGallery;

import 'package:polymer/polymer.dart';
import 'google_drive.dart';

/**
 * A Polymer event-gallery element.
 */
@CustomTag('event-gallery')
class EventGallery extends PolymerElement {
  @published List<String> photos = [];
  @published String currentEventId;

  /// Constructor used to create instance of EventGallery.
  EventGallery.created() : super.created() {
    refresh();
  }
  
  currentEventIdChanged(oldValue, newValue) {
    refresh();
  }
  
  refresh() {
    if (currentEventId == null) return;
    photos = [];
    new GoogleDrive().drive.then((drive) {
      var params = {
          'q': 'trashed = false and not mimeType contains "folder" and "$currentEventId" in parents'
      };
      drive.request('files', 'GET', queryParams: params).then((response) {
        photos = response['items'].map((item) => item['thumbnailLink']).toList();
      });
    });
  }
}
