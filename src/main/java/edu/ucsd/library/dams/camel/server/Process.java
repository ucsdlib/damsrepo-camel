package edu.ucsd.library.dams.camel.server;

import org.apache.camel.Exchange;

/**
 * Our business service.
 * 
 *  @author lsitu@ucsd.edu
 */
public interface Process {

    /**
     * Process the message.
     *
     * @param message from client
     * @return
     */
    void process(Exchange exchange) throws Exception;

}
