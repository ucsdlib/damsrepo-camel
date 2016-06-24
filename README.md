[UC San Diego Library](http://libraries.ucsd.edu/ "UC San Diego Library") DAMS Repository Camel.

# Setup Instructions

1. Check out DAMSPAS from GIT
    ``` sh
    git clone https://github.com/ucsdlib/damsrepo-camel.git
    ```
2. Build

  Complie only

    ``` sh
    mvn compile
    ```

  Run test and build

    ``` sh
    mvn install
    ```

3. Run Server

    ``` sh
    mvn exec:java -PCamelServer
    ```

  To stop hit <kbd>ctrl</kbd>+<kbd>c</kbd>

4. Run Client to create derivatives:

    ``` sh
    mvn exec:java -Dtarget.main.class="edu.ucsd.library.dams.camel.client.DamsCamelClient" -Dtarget.cmd.args="$command $sourceFile $destinationFile $size"
    ```

    ``` sh
    Example: mvn exec:java -Dtarget.main.class="edu.ucsd.library.dams.camel.client.DamsCamelClient" -Dtarget.cmd.args="ffmpeg /tmp/video.avi /tmp/converted.mp4"
    $command: /path/to/ffmpeg | /path/to/convert
    $sourceFile: /path/to/source_file
    $destinationFile: /path/to/derivative_file_to_create
    $size: image size (WxH)
    ```

For more information, please visit http://camel.apache.org.
