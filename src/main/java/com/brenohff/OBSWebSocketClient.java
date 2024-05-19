package com.brenohff;

import com.google.gson.JsonObject;
import io.obswebsocket.community.client.OBSRemoteController;
import io.obswebsocket.community.client.message.event.inputs.InputActiveStateChangedEvent;
import io.obswebsocket.community.client.message.request.sceneitems.GetSceneItemListRequest;
import io.obswebsocket.community.client.message.request.sceneitems.SetSceneItemEnabledRequest;
import io.obswebsocket.community.client.message.response.sceneitems.GetSceneItemListResponse;

public class OBSWebSocketClient {

    private static final String PASSWORD = "QhQ0JKRL3LSIPSSR";

    public static void main(String[] args) {
        new OBSWebSocketClient().start();
    }

    private void start() {
        OBSRemoteController obsRemoteController = OBSRemoteController.builder()
                .host("localhost")                          // Default host
                .port(4455)                                 // Default port
                .password(PASSWORD)                         // Provide your password here
                .connectionTimeout(3)               // Seconds the client will wait for OBS to respond
                .registerEventListener(InputActiveStateChangedEvent.class, (event) -> {
                    System.out.println("Input " + event.getInputName() + " is now " + (event.getVideoActive() ? "active" : "inactive"));
                })
                .lifecycle()                              // Add some lifecycle callbacks
                .onReady(() -> {
                })
                .and()
                .lifecycle()
                .onCommunicatorError((error) -> System.out.println("Error: " + error.getReason()))
                .and()
                .build();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("mode", "capture_specific_window");
        jsonObject.addProperty("window", "[HxOutlook.exe]: Email");

        obsRemoteController.connect();

        obsRemoteController.sendRequest(GetSceneItemListRequest.builder().sceneName("Cena").build(), (GetSceneItemListResponse response) -> {
            if (response.isSuccessful()) {
                response.getSceneItems().stream().filter(
                                sceneItem -> sceneItem.getSourceName().equals("Webcam")).findFirst()
                        .ifPresent(sceneItem -> obsRemoteController.sendRequest(SetSceneItemEnabledRequest.builder().sceneName("Cena").sceneItemId(sceneItem.getSceneItemId())
                                .sceneItemEnabled(true)
                                .build(), (response1) -> {
                            if (response1.isSuccessful()) {
                                System.out.println("Scene item enabled");
                            } else {
                                System.out.println("Failed to enable scene item");
                            }
                        }));
                System.out.println("Scene items: " + response.getSceneItems());
            } else {
                System.out.println("Failed to trigger media input action");
            }
        });

        obsRemoteController.disconnect();
    }

}
