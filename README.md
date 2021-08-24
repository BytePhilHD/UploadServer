# UploadServer
UploadServer is a Java 11 based Application to host a WebSite where Users can upload files and store them with a UUID at your Server.

Currently the upload function is not working!

<h1>How to install</h1>
Download the newest Version from the Releases Page.

**IMPORTANT: Install Java 11, if you havent already**

**Windows:** Create a batch file (e.g. "start.bat") in that you need the Line 
`java -Xmx1G -jar UploadServer.jar`. If you rename your .jar file, type your new Name
instead of "WebTest". 

**Linux:** Create a sh file (e.g. "start.sh") in that you need the Line
`screen -AmdS VidTogether java -Xmx1G -jar UploadServer.jar`. If you havent already we recommend
installing screen (apt install screen), if you dont want to use it, just write the same as on Windows.

Now start the file (Windows doubleclick, Linux ./start.sh). The Programm should start up.
If you're running the Programm at home, you may need to release your Port (standard 80) in your
Routers setting.

Now you can access your Site via localhost (if you changed your Port localhost:YOURPORT) or
use the IP Address instead of localhost
