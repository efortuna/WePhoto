import 'dart:async';
import 'package:polymer/polymer.dart';
import 'package:google_drive_v2_api/drive_v2_api_browser.dart';
import 'package:google_oauth2_client/google_oauth2_browser.dart';

/**
 * A Polymer google-drive element.
 */
@CustomTag('google-drive')
class GoogleDrive {
  static Drive _drive;
  static Token _driveToken;
  static Completer<Drive> _driveCompleter = new Completer();
  static const String CLIENT_ID = "523413454263-c9iql3l99rrog7m3sl7vn61phpkv17h9.apps.googleusercontent.com";
  static final List<String> SCOPES = ["https://www.googleapis.com/auth/drive"];
  
  /// Constructor used to create instance of GoogleDrive.
  GoogleDrive() {
    if (_drive != null) return;
    GoogleOAuth2 auth = new GoogleOAuth2(CLIENT_ID, SCOPES, tokenLoaded: oauthReady, autoLogin: true);
    _drive = new Drive(auth);
  }

  /**
   * Called when authorization server replies with a token.
   */
  void oauthReady(token) {
    _driveToken = token;
    _driveCompleter.complete(_drive);
    print('OAuth complete: $token');
  }
  
  /**
   * Returns the drive object once its fully authenticated.
   */
  Future<Drive> get drive => _driveCompleter.future;
  
}
