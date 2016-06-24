package edu.ucsd.library.dams.camel.server;

import java.util.Map;

import org.apache.camel.Body;
import org.apache.camel.Headers;
import org.apache.camel.OutHeaders;

/**
 * This is the implementation of the business service.
 * @author lsitu@ucsd.edu
 */
public class ErrorResponse {

    /**
     * Error response
     * @param in
     * @param body
     * @param out
     * @return
     */
    public Object processFailed(@Headers Map<?, ?> in, @Body String body, @OutHeaders Map<String, Object> out) {
        out.put("result", false);
        out.put("sourceFile", in.get("sourceFile"));
        out.put("destFile", in.get("destFile"));
        return "Command failed: " + in.get("command") + ".";
    }
}
