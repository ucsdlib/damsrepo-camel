package edu.ucsd.library.dams.camel.server;

import org.apache.camel.builder.RouteBuilder;

/**
 * This class defines the routes on the Server. The class extends a base class in Camel {@link RouteBuilder}
 * that can be used to easily setup the routes in the configure() method.
 * 
 * @author lsitu@ucsd.edu
 */
public class ServiceRoutes extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        /**
         * A generic error handler for this RouteBuilder
         */
        onException(Exception.class)
            .handled(true)
            .bean(ErrorResponse.class, "processFailed")
            .to("mock:error");

        // generic error handler with redeliveries to try
        errorHandler(deadLetterChannel("mock:error").maximumRedeliveries(1));

        // route from the damsrepo queue for processing.
        from("jms:queue:damsrepo").choice()
        .when(header(Constants.COMMAND).endsWith(Constants.COMMAND_FFMPEG))
            .to("ffmpeg")
        .when(header(Constants.COMMAND).endsWith(Constants.COMMAND_IMAGEMAGICK))
            .to("imageMagick")
        .otherwise()
            .to("mock:error");

    }

}
