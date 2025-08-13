package org.happysoft.jukebox.player;

import java.util.ArrayList;
import java.util.List;
import org.happysoft.jukebox.model.Request;


public class Playlist {

  private final List<Request> queue = new ArrayList<>();
  private Request lastRequest = null;
  
  private static final Playlist INSTANCE = new Playlist();

  private Playlist() {
  }

  public static Playlist getPlayList() {
    return INSTANCE;
  }

  public void addSelection(Request request) {
    queue.add(request);
  }

  public void removeSelection(Request request) {
    queue.remove(request);
  }

  public Request getNextSelection() {
    if(!queue.isEmpty()) {
      lastRequest = queue.remove(0);
      return lastRequest;
    }
    lastRequest = null;
    return null;
  }

}