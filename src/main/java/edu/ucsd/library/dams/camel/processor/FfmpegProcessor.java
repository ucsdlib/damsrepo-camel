package edu.ucsd.library.dams.camel.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.util.IOHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * Processor to create thumbnail and video/audio derivatives
 * 
 * @author lsitu@ucsd.edu
 */
public class FfmpegProcessor implements Processor {
	private static Logger LOGGER = Logger.getLogger(FfmpegProcessor.class);

	public void process(Exchange exchange) throws Exception {
		Message message = exchange.getIn();
		String command = (String) message.getHeader("command");
		String srcFile = (String) message.getHeader("sourceFile");
		String destFile = (String) message.getHeader("destFile");
		String params = (String) message.getHeader("params");
		String offset = (String) message.getHeader("offset");

		LOGGER.info("{Command: " + command + ", sourceFile: \"" + srcFile + "\", destFile: \"" + destFile
				+ "\", params: \"" + params + "\" offset: " + offset + "}");
		String messageSufix = "derivative " + destFile + " from " + srcFile + ".";
		try {
			if (!createDerivative ( command, srcFile, destFile, params, offset)){
				throw new Exception("Failed to create " + messageSufix);
			} else {
				message.setHeader("result", true);
				message.setBody("Successfully created " + messageSufix);
			}
		}catch (Exception e) {
			exchange.setException(e);
			message.setHeader("result", false);
			message.setBody("Failed to create " + messageSufix);
			LOGGER.error("Failed to create " + messageSufix, e);
			throw e;
		}
	}

	/**
	 * create thumbnail and vidoe/audio derivative
	 * @param command
	 * @param srcFile - source file id
	 * @param destFile - thumbnail files id
	 * @param codecParams - codec and other params like scale 150:-1, 450:-1, 768:-1 etc.
	 * @param offset - start position in seconds or 00:00:10.xxx format
	 * @return
	 * @throws Exception 
	 */
	public boolean createDerivative (String command, String srcFile, String destFile, String codecParams, String offset)
			throws Exception 
	{
		File src = new File(srcFile);
		if (!src.exists()) {
			throw new FileNotFoundException("Source file doesn't exist: " + src.getAbsolutePath());
		}
		String derFileName = destFile.substring(destFile.lastIndexOf("/") + 1);
		File destTemp = File.createTempFile("ffmpegtmp",derFileName);
		if (destTemp.exists())
			destTemp.delete();
		destTemp.deleteOnExit();

		// build the command
		ArrayList<String> cmd = new ArrayList<String>();
		cmd.add( command );
		cmd.add( "-y" );						// delete silently
		if (StringUtils.isNotBlank(offset)) {
			cmd.add( "-ss" );					// start point of the input video stream
			cmd.add( offset );
		}
		cmd.add( "-i" );
		cmd.add( src.getAbsolutePath() );		// source video file
		if (StringUtils.isNotBlank(codecParams)) 
		{
			List<String> codecParamsList = Arrays.asList(codecParams.split(" "));
			cmd.addAll(codecParamsList);		// codec and other params
		}

		cmd.add( destTemp.getAbsolutePath() );	// temporary converted file

		boolean successful = exec( cmd );

		if ( destTemp.exists() && destTemp.length() > 0 ) {
			// write the thumbnail/derivative created to filestore
			FileInputStream fis = null;
			FileOutputStream fos = null;
			byte[] buf = new byte[5096];
			int bytesRead = 0;
			try{
				File dest = new File( destFile );
				if (dest.exists())
					dest.delete();
				fis = new FileInputStream(destTemp);
				fos = new FileOutputStream(destFile);
				while ( (bytesRead=fis.read(buf)) > 0 ) {
					fos.write(buf, 0, bytesRead);
				}
			}finally {
				IOHelper.close(fis);
				IOHelper.close(fos);
				if (destTemp.exists()) {
					destTemp.delete();
				}
			}
		}
		return successful;
	}

	private boolean exec(List<String> cmd) throws Exception 
	{
		InputStream in = null;
		InputStream inErr = null;
		Process proc = null;
		final StringBuffer log = new StringBuffer();
		int status = -1;
		try {
			// Execute the command
			ProcessBuilder pb = new ProcessBuilder(cmd);
			pb.redirectErrorStream(true);
			proc = pb.start();

			// dump metadata and errors
			in = proc.getInputStream();
			inErr = proc.getErrorStream();
			final BufferedReader buf = new BufferedReader(new InputStreamReader(in));
			final BufferedReader bufErr = new BufferedReader(new InputStreamReader(inErr));

			new Thread() {
				@Override
				public void run()
				{
					try {
						for ( String line = null; (line=bufErr.readLine()) != null; ) {
							log.append( line + "\n" );
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.start();

			new Thread() {
				@Override
				public void run()
				{
					try {
						for ( String line = null; (line=buf.readLine()) != null; ) {
							log.append( line + "\n" );
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.start();

			// Wait for the process to exit
			status = proc.waitFor();
			if ( status == 0 )
				return true;
			else
				throw new Exception( "Error status code: " + status);

		} catch ( Exception ex ) {
			throw new Exception( log.toString(), ex );
		} finally {
			IOHelper.close(in);
			IOHelper.close(inErr);
			if(proc != null){
				proc.destroy();
				proc = null;
			}
		}
	}

}
