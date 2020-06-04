package com.wavemaker.runtime.connector.exception;

/**
 * @author <a href="mailto:sunil.pulugula@wavemaker.com">Sunil Kumar</a>
 * @since 17/4/20
 */
public class ConnectorDoesNotExist extends RuntimeException {

    public ConnectorDoesNotExist(String message) {
        super(message);
    }

    public ConnectorDoesNotExist(String message, Throwable cause) {
        super(message, cause);
    }
}
