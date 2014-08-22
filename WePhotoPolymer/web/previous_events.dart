library WePhoto.PreviousEvents;

import 'package:polymer/polymer.dart';
import 'google_drive.dart';
import 'package:core_elements/core_selector.dart';
import 'package:paper_elements/paper_input.dart';

/**
 * A Polymer previous-events element.
 */
@CustomTag('previous-events')
class PreviousEvents extends PolymerElement {
  @published List<Map> events;
  @published String currentEventId;
  @published String currentEventName;

  /// Constructor used to create instance of EventGallery.
  PreviousEvents.created() : super.created() {
    refresh();
  }
  
  ready() {
    this.$['eventSelector'].on['core-select'].listen((_) {
      if (events == null) return;
      var selected = (this.$['eventSelector'] as CoreSelector).selected;
      if (selected is String) selected = int.parse(selected);
      currentEventId = events[selected]['id'];
      currentEventName = events[selected]['title'];
    });
    
    this.$['createEvent'].on['click'].listen((_) {
      this.$['createDialog'].toggle();
    });
    
    this.$['createDialog'].on['core-overlay-open'].listen((_) {
      this.$['eventName'].focus();
    });
    
    this.$['saveEvent'].on['click'].listen((_) {
      createEvent((this.$['eventName'] as PaperInput).value);
      this.$['createDialog'].toggle();
    });
  }
  
  refresh() {
    events = null;
    currentEventId = null;
    new GoogleDrive().drive.then((drive) {
      var params = {
          'q': 'trashed = false and mimeType contains "folder"'
      };
      drive.request('files', 'GET', queryParams: params).then((response) {
        var items = response['items'];
        items.sort((a, b) => 
                DateTime.parse(b['createdDate']).millisecondsSinceEpoch 
                - DateTime.parse(a['createdDate']).millisecondsSinceEpoch);
        
        events = items.map((item) =>
          {'id': item['id'], 'title': item['title']}).toList();
        currentEventId = events[0]['id'];
      });
    });
  }
  
  createEvent(String eventName) {
    new GoogleDrive().drive.then((drive) {
      var params = {};
      drive.request(
          'files', 'POST',  
          body: '{'
                ' title: "$eventName", '
                ' mimeType: "application/vnd.google-apps.folder"'
                '}', 
          queryParams: params).then((response) {
        refresh();
      });
    });
  }

  /*
   * Optional lifecycle methods - uncomment if needed.
   *

  /// Called when an instance of previous-events is inserted into the DOM.
  attached() {
    super.attached();
  }

  /// Called when an instance of previous-events is removed from the DOM.
  detached() {
    super.detached();
  }

  /// Called when an attribute (such as  a class) of an instance of
  /// previous-events is added, changed, or removed.
  attributeChanged(String name, String oldValue, String newValue) {
  }

  /// Called when previous-events has been fully prepared (Shadow DOM created,
  /// property observers set up, event listeners attached).
  ready() {
  }
   
  */
  
}
