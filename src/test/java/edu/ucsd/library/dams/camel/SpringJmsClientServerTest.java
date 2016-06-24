package edu.ucsd.library.dams.camel;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Producer;
import org.apache.camel.test.AvailablePortFinder;
import org.apache.camel.test.spring.CamelSpringTestSupport;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import edu.ucsd.library.dams.camel.server.Constants;

public class SpringJmsClientServerTest extends CamelSpringTestSupport {

    @BeforeClass
    public static void setupFreePort() throws Exception {
        // find a free port number, and write that in the custom.properties file
        // which we will use for the unit tests, to avoid port number in use problems
        int port = AvailablePortFinder.getNextAvailable();
        String bank1 = "tcp.port=" + port;

        File custom = new File("target/custom.properties");
        FileOutputStream fos = new FileOutputStream(custom);
        fos.write(bank1.getBytes());
        fos.close();
    }

    @Override
    protected AbstractApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext("/META-INF/spring/camel-server.xml");
    }

    @Test
    public void testImageDerivativeCreation() throws Exception {
        // get the endpoint from the camel context
        Endpoint endpoint = context.getEndpoint("jms:queue:damsrepo");

        // create the exchange used for the communication
        // we use the in out pattern for a synchronized exchange where we expect a response
        Exchange exchange = endpoint.createExchange(ExchangePattern.InOut);
        // set the input message
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("image.jpg").getFile());

        org.apache.camel.Message message = exchange.getIn();
        message.setHeader(Constants.SOURCE_FILE, file.getAbsolutePath());
        message.setHeader(Constants.DEST_FILE, file.getParent() + File.separatorChar + "image-converted.jpg");
        message.setHeader(Constants.COMMAND, Constants.COMMAND_IMAGEMAGICK);
        message.setHeader("size", "450x450");

        // to send the exchange we need an producer to do it for us
        Producer producer = endpoint.createProducer();
        // start the producer so it can operate
        producer.start();

        // let the producer process the exchange where it does all the work in this one line of code
        producer.process(exchange);

        // get the response
        boolean response = (Boolean) exchange.getOut().getHeader("result");
        
        assertEquals("Get a wrong response.", true, response);

        // stop the producer after usage
        producer.stop();
    }

    @Test
    public void testVideoDerivativeCreation() throws Exception {
        // get the endpoint from the camel context
        Endpoint endpoint = context.getEndpoint("jms:queue:damsrepo");

        // create the exchange used for the communication
        // we use the in out pattern for a synchronized exchange where we expect a response
        Exchange exchange = endpoint.createExchange(ExchangePattern.InOut);
        // set the input message
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("video.avi").getFile());

        org.apache.camel.Message message = exchange.getIn();
        message.setHeader("CamelJmsRequestTimeout", 360000);
        message.setHeader(Constants.SOURCE_FILE, file.getAbsolutePath());
        message.setHeader(Constants.DEST_FILE, file.getParent() + File.separatorChar + "video-converted.mp4");
        message.setHeader(Constants.COMMAND, Constants.COMMAND_FFMPEG);
        message.setHeader("params", "-vcodec libx264 -pix_fmt yuv420p -profile:v baseline -b:a 192k -crf 18 -vf yadif -threads 2");
        //message.setHeader("command", "/usr/local/bin/ffmpeg");
        //message.setHeader("offset", "500");

        // to send the exchange we need an producer to do it for us
        Producer producer = endpoint.createProducer();
        // start the producer so it can operate
        producer.start();

        // let the producer process the exchange where it does all the work in this one line of code
        producer.process(exchange);

        // get the response
        boolean response = (Boolean) exchange.getOut().getHeader("result");
        
        assertEquals("Get a wrong response.", true, response);

        // stop the producer after usage
        producer.stop();
    }

    @Test
    public void testVideoThumbnailCreation() throws Exception {
        // get the endpoint from the camel context
        Endpoint endpoint = context.getEndpoint("jms:queue:damsrepo");

        // create the exchange used for the communication
        // we use the in out pattern for a synchronized exchange where we expect a response
        Exchange exchange = endpoint.createExchange(ExchangePattern.InOut);
        // set the input message
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("video.mp4").getFile());

        org.apache.camel.Message message = exchange.getIn();
        //message.setHeader("CamelJmsRequestTimeout", 360000);
        message.setHeader(Constants.SOURCE_FILE, file.getAbsolutePath());
        message.setHeader(Constants.DEST_FILE, file.getParent() + File.separatorChar + "video-thumbnail.jpg");
        message.setHeader(Constants.COMMAND, Constants.COMMAND_FFMPEG);
        message.setHeader("params", "-vf thumbnail,scale=450:450 -vframes 1");
        //message.setHeader("offset", "100");

        // to send the exchange we need an producer to do it for us
        Producer producer = endpoint.createProducer();
        // start the producer so it can operate
        producer.start();

        // let the producer process the exchange where it does all the work in this one line of code
        producer.process(exchange);

        // get the response
        boolean response = (Boolean) exchange.getOut().getHeader("result");
        
        assertEquals("Get a wrong response.", true, response);

        // stop the producer after usage
        producer.stop();
    }
}
