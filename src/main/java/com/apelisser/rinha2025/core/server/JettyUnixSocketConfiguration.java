package com.apelisser.rinha2025.core.server;

import org.eclipse.jetty.unixdomain.server.UnixDomainServerConnector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.nio.file.Path;

@Configuration
public class JettyUnixSocketConfiguration {

    @Value("${jetty.server.unix-socket.path}")
    private String unixSocketPath;

    @Value("${jetty.server.unix-socket.accept-queue-size}")
    private int acceptQueueSize;

    @Value("${jetty.server.unix-socket.idle-timeout}")
    private int idleTimeout;

    @Bean
    public WebServerFactoryCustomizer<JettyServletWebServerFactory> jettyWebServerFactoryCustomizer() {
        return factory -> {
            if (StringUtils.hasText(unixSocketPath)) {
                factory.addServerCustomizers(server -> {
                    // Remover conectores HTTP padrao se a intencao e usar apenas UDS.
                    // server.setConnectors(new Connector[0]);

                    // Configurar Unix Domain Socket
                    UnixDomainServerConnector connector = new UnixDomainServerConnector(server);
                    connector.setUnixDomainPath(Path.of(unixSocketPath));

                    // Configuracoes opcionais de performance
                    connector.setIdleTimeout(idleTimeout);
                    connector.setAcceptQueueSize(acceptQueueSize);

                    server.addConnector(connector);
                });
            }
        };
    }

}
