package edu.ucsd.library.dams.camel.server;

import org.apache.camel.PropertyInject;
import org.apache.camel.builder.RouteBuilder;

/**
 * This class defines the routes on the Server. The class extends a base class in Camel {@link RouteBuilder}
 * that can be used to easily setup the routes in the configure() method.
 * 
 * @author lsitu@ucsd.edu
 */
public class ServiceRoutes extends RouteBuilder {

    @PropertyInject("{{redelivery.delay}}")
    private String redeliveryDelay;

    @PropertyInject("{{redelivery.max}}")
    private int redeliveryMax;

    @Override
    public void configure() throws Exception {

        // generic error handler with redeliveries to try
        errorHandler(deadLetterChannel("jms:queue:DLQ.ERROR")
                .maximumRedeliveries(redeliveryMax)
                .delayPattern(redeliveryDelay));

        // route from the damsrepo queue for processing.
        from("jms:queue:damsrepo?requestTimeout={{request.timeout}}&concurrentConsumers={{pool.size}}")
            .choice()
                .when(header(Constants.COMMAND).endsWith(Constants.COMMAND_FFMPEG))
                    .to("ffmpeg")
                .when(header(Constants.COMMAND).endsWith(Constants.COMMAND_IMAGEMAGICK))
                    .to("imageMagick")
                .otherwise()
                    .throwException(new Exception("Unknown command: " + header(Constants.COMMAND)))
            .endChoice();

    }
}
