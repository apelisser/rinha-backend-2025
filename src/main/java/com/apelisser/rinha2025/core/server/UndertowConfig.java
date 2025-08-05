package com.apelisser.rinha2025.core.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UndertowConfig implements WebServerFactoryCustomizer<UndertowServletWebServerFactory> {

    @Value("${undertow.server.threads.io}")
    private Integer ioThreads;

    @Value("${undertow.server.threads.worker}")
    private Integer workerThreads;

    @Value("${undertow.server.buffer-size}")
    private Integer bufferSize;

    @Value("${undertow.server.direct-buffers}")
    private Boolean directBuffers;

    @Value("${undertow.server.customizer}")
    private Boolean customizer;

    @Override
    public void customize(UndertowServletWebServerFactory factory) {
        if (!Boolean.TRUE.equals(customizer)) {
            return;
        }

        if (ioThreads != null) {
            factory.setIoThreads(ioThreads);
        }

        if (workerThreads != null) {
            factory.setWorkerThreads(workerThreads);
        }

        if (bufferSize != null) {
            factory.setBufferSize(bufferSize);
        }

        if (directBuffers != null) {
            factory.setUseDirectBuffers(directBuffers);
        }
    }

}
