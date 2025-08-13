package org.happysoft.jukebox.player;

import jakarta.jms.Connection;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import java.util.List;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.happysoft.jukebox.messaging.AddToQueueMessage;
import org.happysoft.jukebox.messaging.JukeboxMessage;
import org.happysoft.jukebox.messaging.PauseMessage;
import org.happysoft.jukebox.messaging.PlayMessage;
import org.happysoft.jukebox.messaging.StopMessage;
import org.happysoft.jukebox.model.Request;

/**
 *
 * @author chrisf
 */
public class JukeboxMessageReceiver {
  
  private final AudioFilePlayer player;

  private final String brokerUrl = "tcp://localhost:61616";
  private final String queueName = "Jukebox/jms/queue/RequestQueue";

  private final ActiveMQConnectionFactory connectionFactory;
  private final Queue queue;
  private boolean alive = true;

  public JukeboxMessageReceiver(AudioFilePlayer player) {
    this.player = player;
    connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
    connectionFactory.setTrustedPackages(
            List.of("org.happysoft.jukebox.messaging",
                    "org.happysoft.jukebox.model")
    );
    JMSContext context = connectionFactory.createContext();
    queue = context.createQueue(queueName);
  }

  public void startReceiving() {
    try {
      final Connection connection = connectionFactory.createConnection();
      final Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
      final MessageConsumer messageConsumer = session.createConsumer(queue);

      connection.start();
      
      while (alive) {
        final Message jmsMessage = messageConsumer.receive(10000L);
        if (jmsMessage != null) {
          System.out.println("Received message ");
          JukeboxMessage message = jmsMessage.getBody(JukeboxMessage.class);

          if (message instanceof AddToQueueMessage addToQueueMessage) {
            Request req = addToQueueMessage.getRequest();
            System.out.println("adding " + req.getTrack().getTrackName());
            Playlist.getPlayList().addSelection(req);
          }
          if (message instanceof PauseMessage) {
            player.pause();
          }
          if (message instanceof PlayMessage) {
            player.resume();
          }
          if (message instanceof StopMessage) {
            player.stopCurrentRequest();
          }
          jmsMessage.acknowledge();
        }
      }

    } catch (final JMSException e) {
      
    }
  }
  
  public void stop() {
      alive = false;
  }
}
