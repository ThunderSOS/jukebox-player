
package org.happysoft.jukebox.player;

import org.happysoft.jukebox.model.Request;

/**
 *
 * @author chrisf
 */
public class AudioFileEvent {
  
  public static enum PlaybackStatus{
    MEDIA_STARTED, 
    MEDIA_ENDED, 
    MEDIA_STOPPED, 
    MEDIA_PAUSED,
    MEDIA_RESUMED, 
    MEDIA_ERROR
  }
  
  private final Request request;
  private final PlaybackStatus status;
  
  public AudioFileEvent(Request request, PlaybackStatus status) {
    this.request = request;
    this.status = status;
  }
  
  public PlaybackStatus getStatus() {
    return status;
  }
  
  public Request getRequest() {
    return request;
  }
  
}
