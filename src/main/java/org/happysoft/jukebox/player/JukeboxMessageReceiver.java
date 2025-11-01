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
import org.happysoft.jukebox.messaging.RemoveFromQueueMessage;
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

  public void startReceiving(MessageConsumer messageConsumer) throws JMSException {
    while (alive) {
      final Message jmsMessage = messageConsumer.receive(10000L);
      if (jmsMessage != null) {        
        JukeboxMessage message = jmsMessage.getBody(JukeboxMessage.class);
        System.out.println("Received message - " + message.getClass());

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
        if (message instanceof RemoveFromQueueMessage removeFromQueueMessage) {
          Playlist.getPlayList().removeSelection(removeFromQueueMessage.getRequestId());
        }
        jmsMessage.acknowledge();
      }
    }
  }

  public MessageConsumer createConsumer() throws JMSException {
    final Connection connection = connectionFactory.createConnection();
    final Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
    final MessageConsumer messageConsumer = session.createConsumer(queue);
    connection.start();
    System.out.println("JMS connected");
    return messageConsumer;
  }
  
  public void stop() {
      alive = false;
  }
}
