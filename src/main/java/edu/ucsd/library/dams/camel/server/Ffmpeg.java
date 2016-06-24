package edu.ucsd.library.dams.camel.server;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Service;

import edu.ucsd.library.dams.camel.processor.FfmpegProcessor;

/**
 * This is the implementation of the business service.
 * @author lsitu@ucsd.edu
 */
// START SNIPPET: e1
@Service(value = "ffmpeg")
public class Ffmpeg implements Process {

    public void process(final Exchange exchange) throws Exception {
    	Processor processor = new FfmpegProcessor();
        processor.process(exchange);
    }
}
// END SNIPPET: e1