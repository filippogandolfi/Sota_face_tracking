import org.bytedeco.javacpp.Loader
import org.bytedeco.javacpp.opencv_java
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.io.ByteArrayOutputStream
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JOptionPane

//declaration of global variable
var sotaIP = "130.251.13.108"
var noConncted = true
var clientSocket: Socket? = null
val serverSocket = ServerSocket(9000)
var socketSota: Socket?=null

class PhotoAnalizer {

    //class that write displacement on the socket to SOTA
    class move(var x: Int , var y: Int , var f: Int,var facetime:Long): Runnable{
        override fun run() {

            var stopface=System.currentTimeMillis()
            var difference2=stopface-facetime;
            System.out.println("3 ;face;"+difference2+";")
            var startsocket=System.currentTimeMillis()
            socketSota = serverSocket.accept()
            //Generate the Output Streaming
            val socketOut = DataOutputStream(socketSota?.getOutputStream())
            //flag
            socketOut.writeInt(f)
            socketOut.writeInt(x)
            socketOut.writeInt(y)
            socketOut.writeLong(startsocket)

        }
    }
    @Volatile
    private var sotaPort = 8600

    @Throws(Exception::class)

    //Function that receive photo and call detection face algorithm
    fun ReceivePhoto() {

        class Receive(var clientSocket: Socket?) : Runnable {
            override fun run() {

                //init of input socket
                while (noConncted) {
                    try {
                        clientSocket = Socket(sotaIP, sotaPort)
                        noConncted = false
                        println("Socket connection established")
                    } catch (e: Exception) {
                        Thread.sleep(500)
                        println("Wainitg for the the socket connection")
                    }
                }

                //wait until the socket connection is established
                val inputStream = clientSocket?.getInputStream()
                val dataInputStream = DataInputStream(inputStream)
                println("DataSocket done")
                try {
                    var outname=0;
                    while(true){

                    //read data: firstly the size and then the photo
                    val size = dataInputStream.readInt()
//                    println("size = " + size)
                    val time = dataInputStream.readLong()
  //                  println("time = " + time)

                    val data = ByteArray(size)
                    dataInputStream.readFully(data, 0, size)
                    val bis = ByteArrayInputStream(data)
                    val bImage2 = ImageIO.read(bis)
                    var timesocket=System.currentTimeMillis()
//                        val size = 100000
//                        println("size = " + size)
//                        val data = ByteArray(size)
//                        inputStream?.read(data)
//                        //System.out.println("Number: "+ inputStream?.read()!!.toChar())
//
//                        val bis = ByteArrayInputStream(data)
//                        val bImage2 = ImageIO.read(bis)
                    ImageIO.write(bImage2, "jpg", File("output"+outname+".jpg"));
    //                System.out.println("image created")
                        val writer = PrintWriter("socketreceive.txt", "UTF-8")
                    val diffence=timesocket-time
                        println("2 ;socket;"+ diffence +";")

                        //call face detection algorithm
                    var facetime=System.currentTimeMillis()
                    detection(outname,facetime)
                    outname++
                    }
                    //clientSocket?.close()
                } catch (e: Exception) {
                  println("Error: " + e)
                }
            }
            // Create the DataInputStream and AudioFormat necessary to acquire the audio from the microphone
        }

        //initialize object for the class and the thread
        val moveresponser=move(0,0,0,facetime =0)
        val movet=Thread(moveresponser)
        val micrunnable = Receive(clientSocket)
        val micThread = Thread(micrunnable)
        try{
        micThread.start()
        movet.start()
        }
        catch (e: Exception){
            println(e)
        }
    }
}

fun main(){
    try {
        Loader.load(opencv_java::class.java) //class loaded for face detection
       // initIP()
        PhotoAnalizer().ReceivePhoto()
    } catch (e: Exception) {
        println("Exception caught: $e")
    }
}


private fun detection(outputname: Int, facetime: Long) {
    try {
        val classifier = File("lbpcascade_frontalface_improved.xml")
        val faceDetector = CascadeClassifier(classifier.toString())

        //Loading the image from local repo receive from socket
            val b_image = ImageIO.read(File("/home/marco/sota-face-tracking/Facedetector/output" + outputname + ".jpg"))
            //Center of the image
            val center_x = b_image.width / 2
            val center_y = b_image.height / 2
            //Conversion to Mat
            val image = BufferedImage2Mat(b_image)
            var findface=0
            //Face detection
            val faceDetections = MatOfRect()
            faceDetector.detectMultiScale(image, faceDetections)
            for (rect in faceDetections.toArray()) {
                findface=1
                Imgproc.rectangle(
                    image, Point(rect.x.toDouble(), rect.y.toDouble()),
                    Point((rect.x + rect.width).toDouble(), (rect.y + rect.height).toDouble()),
                    Scalar(0.0, 255.0, 0.0)
                )
              //  println("rectangle h = " + rect.height)
                //println("rectangle w = " + rect.width)
                //println("rectangle x = " + rect.x)
                //println("rectangle y = " + rect.y)
                //Center of the rectangle
                val centroid_x = rect.x + rect.width / 2
                val centroid_y = rect.y + rect.height / 2
                //  println("centroid x = $centroid_x")
                //println("centroid y = $centroid_y")
                val centroid_lenght = 5
                //Show the center of the rectangle
                Imgproc.rectangle(
                    image,
                    Point((centroid_x - centroid_lenght / 2).toDouble(), (centroid_y - centroid_lenght / 2).toDouble()),
                    Point((centroid_x + centroid_lenght / 2).toDouble(), (centroid_y + centroid_lenght / 2).toDouble()),
                    Scalar(255.0, 0.0, 0.0)
                )
                //Show the center of the image
                Imgproc.rectangle(
                    image,
                    Point((center_x - centroid_lenght / 2).toDouble(), (center_y - centroid_lenght / 2).toDouble()),
                    Point((center_x + centroid_lenght / 2).toDouble(), (center_y + centroid_lenght / 2).toDouble()),
                    Scalar(255.0, 0.0, 255.0)
                )
                //Distance
                val distance_x = centroid_x - center_x
                val distance_y = centroid_y - center_y
               //println("dist"+distance_x)

                //println("distance x = $distance_x")
                //println("distance y = $distance_y")
                if(distance_x>rect.height/2 || distance_x<-rect.height/2 || distance_y>rect.height/2 || distance_y<-rect.height/2)
                {
                //if the centroid of the center of the camera is outside the the rectangle send displacement to SOTA otherwise set the motion to zero
                PhotoAnalizer.move(distance_x, distance_y,0,facetime).run()
                }else {

                PhotoAnalizer.move(0,0,0,facetime ).run()
                }
            }
        if(findface==0)
        {
            PhotoAnalizer.move(0,0,1,facetime).run()

        }
            // Saving the output image
           val filename = "Output"+outputname+".jpg"
            Imgcodecs.imwrite(" $filename", image)
            //println("Image  saved")
    } catch (e: IOException) {
        println("Error in detection()")
        e.printStackTrace()
    }

}

private fun displayFatalError(message: String) {
    JOptionPane.showMessageDialog(null, message, "Fatal Error", JOptionPane.ERROR_MESSAGE)
}

@Throws(IOException::class)
fun BufferedImage2Mat(image: BufferedImage): Mat {
    val byteArrayOutputStream = ByteArrayOutputStream()
    ImageIO.write(image, "jpg", byteArrayOutputStream)
    byteArrayOutputStream.flush()
    return Imgcodecs.imdecode(MatOfByte(*byteArrayOutputStream.toByteArray()), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED)
}