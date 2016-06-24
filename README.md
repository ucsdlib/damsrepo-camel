# DAMS Repository Camel

### Build
	git clone https://github.com/ucsdlib/damsrepo-camel.git
#### Complie only
	mvn compile
#### Run test and build
	mvn install

### Run Server
	mvn exec:java -PCamelServer

### Run Client to create derivatives:
	mvn exec:java -Dtarget.main.class="edu.ucsd.library.dams.camel.client.DamsCamelClient" -Dtarget.cmd.args="$command $sourceFile $destinationFile $size"

#### Example: mvn exec:java -Dtarget.main.class="edu.ucsd.library.dams.camel.client.DamsCamelClient" -Dtarget.cmd.args="ffmpeg /tmp/video.avi /tmp/converted.mp4"
#### $command: /path/to/ffmpeg | /path/to/convert
#### $sourceFile: /path/to/source_file
#### $destinationFile: /path/to/derivative_file_to_create
#### $size: image size (WxH)

  To stop hit <kbd>ctrl</kbd>+<kbd>c</kbd>

### For more information, please visit http://camel.apache.org.
