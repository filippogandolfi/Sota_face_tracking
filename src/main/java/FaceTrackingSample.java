import java.awt.Color;
import jp.vstone.RobotLib.*;
import jp.vstone.camera.CRoboCamera;
import jp.vstone.camera.CameraCapture;
import jp.vstone.camera.FaceDetectResult;
import org.opencv.core.Core;


public class FaceTrackingSample {

	static final String TAG = "FaceTrackingSample";
	static final int SMILE_POINT = 45;
	
	public static void main(String args[]){
		CRobotUtil.Log(TAG, "Start " + TAG);

		CRobotPose pose;

		CRobotMem mem = new CRobotMem();

		CSotaMotion motion = new CSotaMotion(mem);
		
		CRoboCamera cam = new CRoboCamera("/dev/video0", motion);
		
		if(mem.Connect()){

			motion.InitRobot_Sota();
			
			CRobotUtil.Log(TAG, "Rev. " + mem.FirmwareRev.get());
			

			CRobotUtil.Log(TAG, "Servo On");
			motion.ServoOn();
			

			pose = new CRobotPose();
			pose.SetPose(new Byte[] {1   ,2   ,3   ,4   ,5   ,6   ,7   ,8}	//id
					,  new Short[]{0   ,-900,0   ,900 ,0   ,0   ,0   ,0}				//target pos
					);

			pose.setLED_Sota(Color.BLUE, Color.BLUE, 255, Color.GREEN);
			
			motion.play(pose, 500);
			CRobotUtil.wait(500);
			

			cam.setEnableSmileDetect(true);
			//
			cam.setEnableFaceSearch(true);
			//
			cam.StartFaceTraking();
			//cam.StartFaceDetect();
			

			int a =0;
			while(true){
				FaceDetectResult result = cam.getDetectResult();
				if(result.isDetect()){
					cam.StartMotionDetection();

					pose.setLED_Sota(Color.YELLOW, Color.YELLOW, 255, Color.GREEN);
					motion.play(pose,1000);
					motion.waitEndinterpAll();

					if (a==0){
					pose = new CRobotPose();
					pose.SetPose(new Byte[] {CSotaMotion.SV_L_SHOULDER}	//id
							,  new Short[]{300 }	//target pos
					);
					pose.setLED_Sota(Color.GREEN, Color.GREEN, 255, Color.GREEN);
					motion.play(pose,1000);
					motion.waitEndinterpAll();

					//音声ファイル再生
					//raw　Waveファイルのみ対応


					pose = new CRobotPose();
					pose.SetPose(new Byte[] {CSotaMotion.SV_L_ELBOW}	//id
							,  new Short[]{400 }	//target pos
					);
					pose.setLED_Sota(Color.GREEN, Color.GREEN, 255, Color.GREEN);
					motion.play(pose,1000);
					motion.waitEndinterpAll();

					pose = new CRobotPose();
					pose.SetPose(new Byte[] {CSotaMotion.SV_L_ELBOW}	//id
							,  new Short[]{-500 }	//target pos
					);
					pose.setLED_Sota(Color.GREEN, Color.GREEN, 255, Color.GREEN);
					motion.play(pose,1000);
					motion.waitEndinterpAll();

					pose = new CRobotPose();
					pose.SetPose(new Byte[] {CSotaMotion.SV_L_ELBOW}	//id
							,  new Short[]{400 }	//target pos
					);
					pose.setLED_Sota(Color.GREEN, Color.GREEN, 255, Color.GREEN);
					motion.play(pose,1000);
					motion.waitEndinterpAll();

					pose = new CRobotPose();
					pose.SetPose(new Byte[] {CSotaMotion.SV_L_ELBOW}	//id
							,  new Short[]{-500 }	//target pos
					);
					pose.setLED_Sota(Color.GREEN, Color.GREEN, 255, Color.GREEN);
					motion.play(pose,1000);
					motion.waitEndinterpAll();

					pose = new CRobotPose();
					pose.SetPose(new Byte[] {1   ,2   ,3   ,4   ,5   ,6   ,7   ,8}	//id
							,  new Short[]{0   ,-900,0   ,900 ,0   ,0   ,0   ,0}				//target pos
					);

					pose.setLED_Sota(Color.BLUE, Color.BLUE, 255, Color.GREEN);

					motion.play(pose, 1000);
					CRobotUtil.wait(500);
					}
					a=1;

				}
				else{
					a=0;

					CRobotUtil.Log(TAG, "[Not Detect]");
					for(int i =0;i<5;i++){
					pose.setLED_Sota(Color.GREEN, Color.GREEN, 255, Color.GREEN);
					motion.play(pose, 500);
					pose.setLED_Sota(Color.BLACK, Color.BLACK, 255, Color.GREEN);
					motion.play(pose, 500);}
				}
				CRobotUtil.wait(500);
			}
		}
		motion.ServoOff();
	}
}
