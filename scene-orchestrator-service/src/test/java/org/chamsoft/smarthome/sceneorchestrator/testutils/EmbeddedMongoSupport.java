package org.chamsoft.smarthome.sceneorchestrator.testutils;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

import java.io.IOException;
import java.net.ServerSocket;

public final class EmbeddedMongoSupport {

    private static final MongodStarter starter = MongodStarter.getDefaultInstance();
    private static MongodExecutable executable;
    private static MongodProcess process;
    private static int port;

    private EmbeddedMongoSupport() {
    }

    public static synchronized int port() {
        if (process != null) {
            return port;
        }

        port = findFreePort();

        try {
            MongodConfig config = MongodConfig.builder()
                    .version(Version.Main.PRODUCTION)
                    .net(new Net("localhost", port, Network.localhostIsIPv6()))
                    .build();

            executable = starter.prepare(config);
            process = executable.start();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start embedded MongoDB", e);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(EmbeddedMongoSupport::stop));
        return port;
    }

    private static int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to find a free port", e);
        }
    }

    public static synchronized void stop() {
        if (process != null) {
            process.stop();
            process = null;
        }
        if (executable != null) {
            executable.stop();
            executable = null;
        }
    }
}

