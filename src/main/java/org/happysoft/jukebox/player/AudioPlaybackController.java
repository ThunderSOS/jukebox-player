package org.happysoft.jukebox.player;

import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import java.util.List;
import org.happysoft.jukebox.model.Request;
import org.happysoft.jukebox.model.RequestStatus;
import static org.happysoft.jukebox.player.AudioFileEvent.PlaybackStatus.MEDIA_ENDED;
import static org.happysoft.jukebox.player.AudioFileEvent.PlaybackStatus.MEDIA_ERROR;
import static org.happysoft.jukebox.player.AudioFileEvent.PlaybackStatus.MEDIA_STARTED;

/**
 *
 * @author chrisf
 */
public class AudioPlaybackController implements AudioPlaybackListener {

  private final AudioFilePlayer player = new AudioFilePlayer();

  private JukeboxMessageReceiver jmsReceiver;
  private JukeboxboxRestClient restClient;

  public void start() {
    player.addMediaListener(this);
    player.start();
    restClient = new JukeboxboxRestClient();
    restClient.clearPlaying();
    List<Request> queue = restClient.getQueue();
    System.out.println("Found " + queue.size() + " queued tracks");
    for (Request req : queue) {
      Playlist.getPlayList().addSelection(req);
    }
    jmsReceiver = new JukeboxMessageReceiver(player);
    
    while(true) {
      try {
        MessageConsumer consumer = jmsReceiver.createConsumer();
        jmsReceiver.startReceiving(consumer);

      } catch (JMSException jmse) {
        jmse.printStackTrace();
        System.out.println("JMSException caught, " + jmse.getMessage());
        System.out.println("Sleep for 10 seconds then rety");
        try {
          Thread.sleep(10000L);
        } catch (InterruptedException ie) {
          System.out.println("Thread interrupted, exiting");
          System.exit(-1);
        }
      }

    } 
  }

  @Override
  public void notifyMediaEvent(AudioFileEvent event) {
    System.out.println("AudioEvent - " + event.getStatus());
    switch (event.getStatus()) {
      case MEDIA_STARTED ->
        restClient.updateRequestSetStatus(event.getRequest().getRequestId(), RequestStatus.PLAYING);
      case MEDIA_ENDED ->
        restClient.updateRequestSetStatus(event.getRequest().getRequestId(), RequestStatus.PLAYED);
      case MEDIA_ERROR ->
        restClient.updateRequestSetStatus(event.getRequest().getRequestId(), RequestStatus.PLAYED);
      case MEDIA_STOPPED ->
        restClient.updateRequestSetStatus(event.getRequest().getRequestId(), RequestStatus.PLAYED);
    }
  }

}
