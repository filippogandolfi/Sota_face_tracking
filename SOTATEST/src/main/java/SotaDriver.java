import jp.vstone.RobotLib.CSotaMotion;
import jp.vstone.RobotLib.CRobotUtil;
import jp.vstone.RobotLib.*;
import jp.vstone.camera.CameraCapture;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import static jp.vstone.camera.CameraCapture.*;
public class SotaDriver{
    public static void main(String args[]) throws FileNotFoundException, UnsupportedEncodingException {

        init(); //connection
        init2(); //after connection Sota waves its hand
        Receive receive=new Receive();
        SendVideo sendervideo = new SendVideo();
        sendervideo.start();
        receive.start();
//                Search search=new Search();
//                search.start();
    }
    static class SendVideo extends Thread {
        public void run() {
            //Initialize variables needed for photo acquisition
            CRobotMem mem = new CRobotMem();
            CSotaMotion motion = new CSotaMotion(mem);
            CameraCapture cap = new CameraCapture(CAP_IMAGE_SIZE_VGA,CAP_FORMAT_3BYTE_BGR);
            try {
                // Open the socket


                cap.openDevice("/dev/video0");
                ServerSocket serverSocket = new ServerSocket(8600);//9980);
                // Wait until the the connection is established
                Socket server = serverSocket.accept();
                OutputStream outputStream = server.getOutputStream();
                while (true) {
                    //acquire,save the photo and convert i to byte to send it by socket

                 //    cap.snapGetFile("fotob.jpg");
                    //BufferedImage image = ImageIO.read(new File("/home/root/fotob.jpg"));
//                                        byte[] imagebyte = cap.getImageRawData();
//                                        InputStream in = new ByteArrayInputStream(imagebyte);
//
//                                        BufferedImage image2 = ImageIO.read(in);
//                                        File test = new File("output.jpg");
//                                        ImageIO.write(image2, "jpg", test);


                    //byte [] image =cap.getImageRawData();
//                    BufferedImage image = cap.RawtoBufferedImage();
                    //System.out.println("class imabeByte: "+imagebyte.getClass().getName());
                    //long diff=fine_time-inizio_time;
                                        /*prova compressione
                                        File compressedImageFile = new File("compressed_image.jpg");
                                        OutputStream os = new FileOutputStream(compressedImageFile);
                                        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
                                        ImageWriter writer = (ImageWriter) writers.next();
                                        ImageOutputStream ios = ImageIO.createImageOutputStream(os);
                                        writer.setOutput(ios);
                                        ImageWriteParam param = writer.getDefaultWriteParam();
                                        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                                        param.setCompressionQuality(0.05f);  // Change the quality value you prefer
                                        writer.write(null, new IIOImage(image, null, null), param);
                                        BufferedImage image2 = ImageIO.read(new File("/home/root/compressed_image.jpg"));
                                        // fine prova compressione*/
                    long inizio_time =System.currentTimeMillis();
                    cap.snap();
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    ImageIO.setUseCache(false);
                    ImageIO.write(cap.RawtoBufferedImage(),"jpg",byteArrayOutputStream);

                    //System.out.println("imagecapture");
                    //System.out.println("image size = " + byteArrayOutputStream.toByteArray().getClass());
                    long socket_time =System.currentTimeMillis();
                    DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                    dataOutputStream.writeInt(byteArrayOutputStream.toByteArray().length);

                    dataOutputStream.writeLong(socket_time);
                    dataOutputStream.write(byteArrayOutputStream.toByteArray());
                    long fine_time =System.currentTimeMillis();
                    long diff=fine_time-inizio_time;
                    System.out.println("1 ;sota image;" + diff +";");



                    //outputStream.write(byteArrayOutputStream.toByteArray(),0,byteArrayOutputStream.toByteArray().length);
                    //server.close();
                    //System.out.println("size sent");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    static class Receive extends Thread {
        public void run() {
            try {
                //init of objet for SOTA


                CRobotPose pose;
                CRobotMem mem = new CRobotMem();
                CSotaMotion motion = new CSotaMotion(mem);
                if(mem.Connect()){
                    motion.InitRobot_Sota();
                    motion.ServoOn();
                    //VARIABLES
                    double X=0;
                    double Y=0;
                    int nodetect=0;
                    double GAIN = 0.4;
                    double GAIN2 = 0.26;
                    int destra=1;
                    short newa=0;
                    while(true) {
                        //Accept the socket connection
                        Socket socket = new Socket("130.251.13.132", 9000);
                        //Generate the Input Streaming and the Input Data Stream
                        InputStream socketIn = socket.getInputStream();
                        DataInputStream dis = new DataInputStream(socketIn);
                        //Receive via socket protocol the size of x and y displacement
                        //flag=1 --> not detect
                        //flag=0 --> detect
                        int flag = dis.readInt();
                      //  System.out.println("flag = " + flag);
                        int Xnew = dis.readInt();
                        //System.out.println("x displacement = " + Xnew);
                        int Ynew = dis.readInt();
                        long time = dis.readLong();
                        //System.out.println("y displacement = " + Ynew);
                        long finesocket=System.currentTimeMillis();
                        long diff=finesocket-time;
                      // writer.println("4 ;socket return;" + diff +";");
                        System.out.println("4 ;socket return;" + diff +";");
                        double Xg = -Xnew * GAIN + X;
                        double Yg = Ynew * GAIN2 + Y;
                        short Xp;
                        Short step=60 ;
                        //cast variable to short
                        short x = (short) (double) Xg;
                        short y = (short) (double) Yg;
                        //System.out.println("y new = " + Yg);
                        //System.out.println("x new = " + Xg);
                        //System.out.println("y, x = " + y + x);
                        if (flag == 1) {
                            newa=x;
                            if (nodetect > 6) {
                                if(destra==1) {
                                    pose = new CRobotPose();
                                    newa=(short)(newa+step);
                                    //System.out.println("newa = " + newa);
                                    pose.SetPose(new Byte[]{CSotaMotion.SV_HEAD_Y,CSotaMotion.SV_HEAD_P}, new Short[]{newa,-150});
                                    pose.setLED_Sota(Color.PINK, Color.PINK, 200, Color.YELLOW);
                                    motion.play(pose, 300);
                                    motion.waitEndinterpAll();
                                    //System.out.println("newa2 = " + newa);
                                    // newa=(short)(newa+step);
                                    if(newa>800){

                                        destra=0;

                                    }
                                }else{
                                    pose = new CRobotPose();
                                    newa=(short)(newa-step);
                                    pose.SetPose(new Byte[]{CSotaMotion.SV_HEAD_Y}, new Short[]{newa});
                                    pose.setLED_Sota(Color.PINK, Color.PINK, 200, Color.YELLOW);
                                    motion.play(pose, 300);
                                    //System.out.println("newa3 = " + newa);
                                    motion.waitEndinterpAll();
                                    if(newa<-800){
                                        destra=1;
                                    }
                                }
                                X=newa;
                                Y=-150;
                            } else {
                                nodetect++;
                            }
                        }
                        else {
                            nodetect = 0;
                            long start_move = System.currentTimeMillis();
                            pose = new CRobotPose();
                            pose.SetPose(new Byte[]{CSotaMotion.SV_HEAD_P, CSotaMotion.SV_HEAD_Y}, new Short[]{y, x});
                            pose.setLED_Sota(Color.BLUE, Color.BLUE, 200, Color.YELLOW);
                            motion.play(pose, 350);
                            X = Xg;
                            Y = Yg;
                        }
                        //update old position
                        // socket.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("failed");
            }
        }
    }

    private static void init2() {
        CRobotPose start_pose;
        CRobotPose pose;
        CRobotMem mem = new CRobotMem();
        CSotaMotion motion = new CSotaMotion(mem);
        if (mem.Connect()) {
            motion.InitRobot_Sota();
            //CRobotUtil.Log("Initial pose", "Rev. " + mem.FirmwareRev.get());
            //CRobotUtil.Log("Initial pose", "Servo On");
            motion.ServoOn();
            start_pose = new CRobotPose();
            start_pose.SetPose(new Byte[]{1, 2, 3, 4, 5, 6, 7, 8}    //id
                    , new Short[]{0, -900, 0, 900, 0, 0, 0, 0}                //target pos
            );
            start_pose.setLED_Sota(Color.CYAN, Color.CYAN, 255, Color.GREEN);
            //CRobotUtil.Log("Initial pose", "play:" + motion.play(start_pose, 500));
            motion.waitEndinterpAll();
            pose = new CRobotPose();
            pose.SetPose(new Byte[]{CSotaMotion.SV_HEAD_R, CSotaMotion.SV_L_SHOULDER,CSotaMotion.SV_L_ELBOW}    //id
                    , new Short[]{400,300,300}    //target pos
            );
            pose.setLED_Sota(Color.CYAN, Color.CYAN, 255, Color.GREEN);
            motion.play(pose, 300);
            motion.waitEndinterpAll();
            CRobotUtil.wait(200);
            for (int j=0; j<4; j++) {
                if (j==0 || j==2 ) {
                    pose = new CRobotPose();
                    pose.SetPose(new Byte[]{CSotaMotion.SV_L_ELBOW}    //id
                            , new Short[]{-250}    //target pos
                    );
                    pose.setLED_Sota(Color.CYAN, Color.CYAN, 255, Color.GREEN);
                    motion.play(pose, 250);
                    motion.waitEndinterpAll();
                    CRobotUtil.wait(200);
                }
                else {
                    pose = new CRobotPose();
                    pose.SetPose(new Byte[] {CSotaMotion.SV_L_ELBOW}   //id
                            ,  new Short[]{250}    //target pos
                    );
                    pose.setLED_Sota(Color.CYAN, Color.CYAN, 255, Color.GREEN);
                    motion.play(pose,250);
                    motion.waitEndinterpAll();
                    CRobotUtil.wait(200);
                }
            }
            motion.play(start_pose,650);
            motion.waitEndinterpAll();
            CRobotUtil.wait(200);
            motion.ServoOff();
        }
    }

    private static void init(){
        new Thread(() -> {
        }).start();
    }
}
