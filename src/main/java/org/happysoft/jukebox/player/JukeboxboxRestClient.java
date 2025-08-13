package org.happysoft.jukebox.player;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.happysoft.jukebox.model.Request;
import org.happysoft.jukebox.model.RequestStatus;

import org.glassfish.jersey.jackson.JacksonFeature;

/**
 *
 * @author chrisf
 */
public class JukeboxboxRestClient {

  private final Client client;

  public JukeboxboxRestClient() {
    client = ClientBuilder.newBuilder().register(JacksonFeature.class).build();
  }

  public List<Request> getQueue() {
    return client.target("http://localhost:8080/Jukebox/api/playlist/getQueue")
            .request(MediaType.APPLICATION_JSON)
            .get(new GenericType<List<Request>>() {
            });
  }

  public void updateRequestSetStatus(long requestId, RequestStatus status) {
    client.target("http://localhost:8080/Jukebox/api/playlist?requestId=" + requestId + "&status=" + status.name())
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json(""), Void.class);
  }

  public void clearPlaying() {
    client.target("http://localhost:8080/Jukebox/api/playlist/clearPlaying")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json(""), Void.class);
  }

}
