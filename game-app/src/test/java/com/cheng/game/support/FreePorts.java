package com.cheng.game.support;

import java.io.IOException;
import java.net.ServerSocket;

public final class FreePorts {

    private FreePorts() {
    }

    public static int next() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to allocate free port", e);
        }
    }
}
