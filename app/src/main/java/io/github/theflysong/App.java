package io.github.theflysong;

import io.github.theflysong.client.ClientApp;

public class App {
    public static ClientApp clientApp;
    public static final String APPID = "linklink";

    public static void main(String[] args) {
        clientApp = new ClientApp();
        clientApp.run();
    }
}