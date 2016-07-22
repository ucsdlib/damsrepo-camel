package edu.ucsd.library.dams.camel.processor;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.util.IOHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * Processor to generate derivatives with ImageMagic convert
 * @author lsitu@ucsd.edu
**/
public class ImageMagickProcessor implements Processor {

	private static Logger LOGGER = Logger.getLogger(ImageMagickProcessor.class);

	public void process(Exchange exchange) throws Exception {
		Message message = exchange.getIn();
		String command = (String) message.getHeader("command");
		String srcFile = (String) message.getHeader("sourceFile");
		String destFile = (String) message.getHeader("destFile");
		String size = (String) message.getHeader("size");
		String frame = (String) message.getHeader("frame");
		int frameNo = -1;
		if (StringUtils.isNotBlank(frame))
			frameNo = Integer.valueOf(frame);

		LOGGER.info("{Command: " + command + ", sourceFile: \"" + srcFile + "\", destFile: \"" + destFile
				+ "\", size: \"" + size + "\" frame: " + frameNo + "}");
		String messageSufix = "derivative " + destFile + " from " + srcFile + ".";
		try{
			if (!createDerivative( command, srcFile, destFile, size, frameNo )) {
				throw new Exception("Failed to create " + messageSufix);
			} else {
				message.setHeader("result", true);
				message.setBody("Successfully created " + messageSufix);
			}
		}catch (Exception e) {
			message.setHeader("result", false);
			message.setBody("Failed to create " + messageSufix);
			LOGGER.error("Failed to create " + messageSufix, e);
			throw e;
		}
	}

	/**
 	 * Generate a derivative image.
	 * @param command - path to ImageMagick convert command
	 * @param srcFile - File for master image.
	 * @param destFile - File for generated derivative.
	 * @param size - size (WxH) of derivative image.
	 * @param frameNo, frame use to create the derivative image.
	 * @throws Exception if an error occurs retrieving master file,
	 *   generating derivative, or storing derivative file.
	**/
	public boolean createDerivative( String command, String srcFile, String destFile, String size, int frameNo )
			throws Exception
	{
		boolean status = false;
		File src = new File(srcFile);
		if (!src.exists()) {
			throw new FileNotFoundException("Source file doesn't exist: " + src.getAbsolutePath());
		}
		String derFileName = destFile.substring(destFile.lastIndexOf("/") + 1);
		File destTemp = File.createTempFile("magicktmp",derFileName);
		destTemp.deleteOnExit();
		boolean gen = createDerivative( command, src, destTemp, size, frameNo );
		if ( gen )
		{
			// write the derivative into filestore
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
				
				status = true;
			}finally {
				IOHelper.close(fis);
				IOHelper.close(fos);
				if (destTemp.exists()) {
					destTemp.delete();
				}
			}
		}

		// return status
		return status;
	}
	
	/* generate a derivative image for a specific page using image magick */
	private boolean createDerivative( String command, File src, File dst, String size, int frameNo )
		throws Exception
	{
		// build the command
		ArrayList<String> cmd = new ArrayList<String>();
		cmd.add( command );
		cmd.add( "-auto-orient" ); // auto-rotate images according to metadata

		if (src.getName().toLowerCase().endsWith(".pdf"))
			cmd.add( "-flatten" );
		cmd.add( "-resize" );      // resize to specified pixel dimensions
		cmd.add( size );
		cmd.add( src.getAbsolutePath() + (frameNo!=-1?"[" + frameNo + "]":"") );
		cmd.add( dst.getAbsolutePath() );

		StringBuffer log = new StringBuffer();
		Reader reader = null;
		InputStream in = null;
		BufferedReader buf = null;
		Process proc = null;
		try
		{
			// execute the process and capture stdout messages
			ProcessBuilder pb = new ProcessBuilder(cmd);
			pb.redirectErrorStream(true);
			proc = pb.start();
			
			in = proc.getInputStream();
			reader = new InputStreamReader(in);
			buf = new BufferedReader(reader);
			for ( String line = null; (line=buf.readLine()) != null; )
			{
				log.append( line + "\n" );
			}

			// wait for the process to finish
			int status = proc.waitFor();
			if ( status == 0 )
			{
				return true;
			}
			else
			{
				IOHelper.close(in);
				IOHelper.close(reader);
				IOHelper.close(buf);
				// capture any error messages
				in = proc.getErrorStream();
				reader = new InputStreamReader(in);
				buf = new BufferedReader(reader);
				for ( String line = null; (line=buf.readLine()) != null; )
				{
					log.append( line + "\n" );
				}
				throw new Exception( log.toString() );
			}
		}
		catch ( Exception ex )
		{
			throw new Exception( log.toString(), ex );
		}finally{
			IOHelper.close(in);
			IOHelper.close(reader);
			IOHelper.close(buf);
			if(proc != null){
				proc.destroy();
				proc = null;
			}
		}
	}

}
