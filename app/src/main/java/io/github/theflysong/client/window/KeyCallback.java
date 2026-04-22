package io.github.theflysong.client.window;

public interface KeyCallback {
    void onKey(long window, int key, int scancode, int action, int mods);
}
