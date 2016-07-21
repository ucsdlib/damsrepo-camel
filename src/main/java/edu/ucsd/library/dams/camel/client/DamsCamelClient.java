package edu.ucsd.library.dams.camel.client;

import java.io.File;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Producer;
import org.apache.camel.util.IOHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import edu.ucsd.library.dams.camel.server.Constants;

/**
 * A test client that uses the Mesage Endpoint pattern to easily exchange messages with the Server.
 * Requires that the JMS broker is running, as well as CamelServer
 */
public final class DamsCamelClient {
    private static final Logger LOGGER = Logger.getLogger(DamsCamelClient.class);
	private DamsCamelClient() {}

    public static void main(final String[] args) throws Exception {
        LOGGER.info("\n\nNotice the client requires that the CamelServer is already running!");

        if (args.length < 3) {
        	LOGGER.error("Wrong number of parameters for command, source file, destination file ...");
        	System.exit(0);
        }
 
        final String command = args[0];
        final File sourceFile = new File(args[1]);
        final File destFile = new File(args[2]);
        
        String size = "";
        if (args.length > 3)
            size = args[3];

        LOGGER.info("Command: " + command + "; sourceFile: " + sourceFile + "; destFile: " + destFile);
        AbstractApplicationContext absContext = new ClassPathXmlApplicationContext("camel-client.xml");
        CamelContext context = absContext.getBean("camel-client", CamelContext.class);

        // get the endpoint from the camel context
        Endpoint endpoint = context.getEndpoint("jms:queue:damsrepo");

        // create the exchange used for the communication
        // we use the in out pattern for a synchronized exchange where we expect a response
        Exchange exchange = endpoint.createExchange(ExchangePattern.InOut);
        // set the input message
        org.apache.camel.Message message = exchange.getIn();
        message.setHeader(Constants.CAMEL_JMS_TIMEOUT, 3600000);
        message.setHeader(Constants.COMMAND, command);
        message.setHeader(Constants.SOURCE_FILE, sourceFile.getAbsolutePath());
        message.setHeader(Constants.DEST_FILE, destFile.getAbsolutePath());

        if (command.endsWith(Constants.COMMAND_FFMPEG)) {
            if (destFile.getName().toLowerCase().endsWith(".jpg"))
                message.setHeader("params", "-vf thumbnail,scale=450:450 -vframes 1");
            else
                message.setHeader("params", "-vcodec libx264 -pix_fmt yuv420p -profile:v baseline -b:a 192k -crf 18 -vf yadif -threads 2");
        } else if (command.endsWith(Constants.COMMAND_IMAGEMAGICK)){
            if (StringUtils.isBlank(size))
                size = "768x768";
            message.setHeader("size", size);
        } else {
            LOGGER.warn("Unknown command: " + command);
            System.exit(0);
        }

        // to send the exchange we need an producer to do it for us
        Producer producer = endpoint.createProducer();
        // start the producer so it can operate
        producer.start();

        // let the producer process the exchange where it does all the work in this one line of code
        producer.process(exchange);

        // get the response from the out body message
        String response = exchange.getOut().getBody(String.class);
 
        LOGGER.info("\n\nProcess result is " + exchange.getOut().getHeader("result") + ": " + response + "\n");
        String result = "" + exchange.getOut().getHeader("result");
        if (StringUtils.isBlank(result) || !Boolean.valueOf(result)) {
            LOGGER.error("Execute failed. Result " + result);
            if(exchange.getException() != null)
            exchange.getException().printStackTrace();
        }

        // stopping the JMS producer has the side effect of the "ReplyTo Queue" being properly
        // closed, making this client not to try any further reads for the replies from the server
        producer.stop();

        // we're done so let's properly close the application context
        IOHelper.close(absContext);
    }
}
