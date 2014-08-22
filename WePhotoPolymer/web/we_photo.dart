import 'package:polymer/polymer.dart';

/**
 * A Polymer we-photo element.
 */
@CustomTag('we-photo')
class WePhoto extends PolymerElement {
  @published int selected = 0;
  @published String currentEventId;
  @published String currentEventName;
  bool firstSelectedEvent = true;

  /// Constructor used to create instance of WePhoto.
  WePhoto.created() : super.created() {
  }
  
  currentEventIdChanged() {
    if (firstSelectedEvent) {
      firstSelectedEvent = false;
      return;
    }
    // TODO: fix the behavior here, we don't actually want to always change here, only if
    // somebody actually clicks on an event in the events page.
//    this.$['pages'].selected = 1;
  }

  /*
   * Optional lifecycle methods - uncomment if needed.
   *

  /// Called when an instance of we-photo is inserted into the DOM.
  attached() {
    super.attached();
  }

  /// Called when an instance of we-photo is removed from the DOM.
  detached() {
    super.detached();
  }

  /// Called when an attribute (such as  a class) of an instance of
  /// we-photo is added, changed, or removed.
  attributeChanged(String name, String oldValue, String newValue) {
  }

  /// Called when we-photo has been fully prepared (Shadow DOM created,
  /// property observers set up, event listeners attached).
  ready() {
  }
   
  */
  
}
