package edu.ucsd.library.dams.camel.server;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Service;

import edu.ucsd.library.dams.camel.processor.ImageMagickProcessor;

/**
 * This is the implementation of the business service.
 * @author lsitu@ucsd.edu
 */
@Service(value = "imageMagick")
public class ImageMagick implements Process {

    public void process(Exchange exchange) throws Exception {
    	Processor processor = new ImageMagickProcessor();
        processor.process(exchange);;
    }
}