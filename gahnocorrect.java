import android.hardware.Camera;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.matrices.VectorF;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;
import org.openftc.revextensions2.ExpansionHubEx;

@Autonomous(name="LOL", group ="Concept")
public class gahnocorrect extends LinearOpMode {

    private DcMotor RF,RB,LF,LB,FI,Cranemotor;
    private CRServo Crane1 = null;
    //  private Servo Arm2 = null;
    private int Where =0;


    BNO055IMU imu;
    private Orientation angles;

    double ticksPerMotorRev = 383.6;
    double driveGearReduction = 2;
    double wheelDiameterInches = 4;
    double ticksPerInch = (ticksPerMotorRev * driveGearReduction) / (wheelDiameterInches * 3.14159265359);
    // IMPORTANT: If you are using a USB WebCam, you must select CAMERA_CHOICE = BACK; and PHONE_IS_PORTRAIT = false;
    private static final String VUFORIA_KEY =
            "AXxGX4//////AAABmaGzhDtLe0vztwUuFzptF78cmvdtkCQa4TLJaygK9Mued0mzNi3KaHkbVeeN1llvJDgiTItqnqEHP1SosYrZk3gZ948OKIw39IGN9dy+MV2AbXcAZEgkl26O6oK+Fr5728OXW75g04pt4+DRuf4GiUQgr6gBjJg0nbRV/7VzlYLwXHKrOK5SJ9rLugJ/rwsw1aVfJAwamNf4YNIaSh3SQgw0dL+nALMxEOC9Hb8aPSijZkW66JMgOz9bYJXZlJUGtRTodc8xes544zLyRNQx5j5aa0onYRADaqtcoNF2bw7PtgZCt0uDHJa+J1+5RZF0IS4X+Otj5VyxOC2z9kAMtbeLG90n71dYmRGgbAAk1DhO";

    // Since ImageTarget trackables use mm to specifiy their dimensions, we must use mm for all the physical dimension.
    // We will define some constants and conversions here



    // Class Members
    private VuforiaLocalizer vuforia = null;
    private ElapsedTime timer = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);

    /**
     * This is the webcam we are to use. As with other hardware devices such as motors and
     * servos, this device is identified using the robot configuration tool in the FTC application.
     */
    WebcamName webcamName = null;



    @Override public void runOpMode() {
        RF = hardwareMap.dcMotor.get("RF"); //gets RFM on hardware map
        RB = hardwareMap.dcMotor.get("RB"); //gets RBM on hardware map
        LF = hardwareMap.dcMotor.get("LF"); //gets LFM on hardware map
        LB = hardwareMap.dcMotor.get("LB"); //gets LBM on hardware map
        FI = hardwareMap.dcMotor.get("FI"); //gets Front Intake on hardware map
        Cranemotor = hardwareMap.dcMotor.get("LIFT");
        //    Arm2 = hardwareMap.servo.get("ARM2");
        Crane1  = hardwareMap.crservo.get("BOOM");
        FI.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        Cranemotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);


        int oneRev = 28 * 40;

        Camera cam = Camera.open();
        Camera.Parameters p = cam.getParameters();
        p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
//        cam.setParameters(p);
//        cam.startPreview();
        //     Arm = hardwareMap.servo.get("ARM");

        RB.setDirection(DcMotor.Direction.REVERSE); //sets both left side motors on reverse
        LF.setDirection(DcMotor.Direction.FORWARD);
        LB.setDirection(DcMotor.Direction.FORWARD);

        RF.setDirection(DcMotor.Direction.REVERSE);

        /*
         * Configure Vuforia by creating a Parameter object, and passing it to the Vuforia engine.
         * We can pass Vuforia the handle to a camera preview resource (on the RC phone);
         * If no camera monitor is desired, use the parameter-less constructor instead (commented out below).
         */
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters(cameraMonitorViewId);

        // VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        parameters.vuforiaLicenseKey = VUFORIA_KEY;

        /*
         * We also indicate which camera on the RC we wish to use.
         */
        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;

        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);

        // Load the data sets for the trackable objects. These particular data
        // sets are stored in the 'assets' part of our application.
        VuforiaTrackables targetsSkyStone = this.vuforia.loadTrackablesFromAsset("Skystone");
        VuforiaTrackable stoneTarget = targetsSkyStone.get(0);
        stoneTarget.setName("Stone Target");



        // WARNING:
        // In this sample, we do not wait for PLAY to be pressed.  Target Tracking is started immediately when INIT is pressed.
        // This sequence is used to enable the new remote DS Camera Preview feature to be used with this sample.
        // CONSEQUENTLY do not put any driving commands in this loop.
        // To restore the normal opmode structure, just un-comment the following line:

             telemetry.addLine("ready to go!");
           telemetry.update();

        waitForStart();
        while (opModeIsActive()) {

            RF.setPower(.2);
            RB.setPower(.2);
            LF.setPower(.2);
            LB.setPower(.2);
            sleep(1200);

            RF.setPower(0);
            RB.setPower(0);
            LF.setPower(0);
            LB.setPower(0);
            sleep(500);


//        RF.setPower(1);
//        RB.setPower(1);
//        LF.setPower(1);
//        LB.setPower(1);
//        sleep(500);

//        RF.setPower(1);
//        RB.setPower(1);
//        LF.setPower(-1);
//        LB.setPower(-1);
//        sleep(250);

            // Note: To use the remote camera preview:
            // AFTER you hit Init on the Driver Station, use the "options menu" to select "Camera Stream"
            // Tap the preview window to receive a fresh image.

            targetsSkyStone.activate();
            while (!isStopRequested()) {
                //Just  a refresh timer, don't actually need this
                if(timer.milliseconds() == 3) continue;
                boolean stoneVisible = false;






                // check to see if the skystone is visible.
                if (((VuforiaTrackableDefaultListener) stoneTarget.getListener()).isVisible()) {
                    telemetry.addData("Visible Target", stoneTarget.getName()); //just returns "Stone Target"
                /*In these coordinates, the X axis goes from the left (negative) to the right (positive).
                    The Y axis goes up and down on the middle of the screen, and the Z axis goes from the camera outward. */

                    //command to get the relative position as provided by vuforia
                    OpenGLMatrix location = ((VuforiaTrackableDefaultListener) stoneTarget.getListener()).getVuforiaCameraFromTarget();
                    if (location != null) {
                        // Get the positional part of the coordinates
                        VectorF translation = location.getTranslation();
                        //clip the actual X to see if it is closer to the left or right
                        float closestX = Range.clip(translation.get(0), -20f, 20f);
                    /*"center" because we (my team) only looks at the right two in the farthest set of three in the quarry,
                    so the leftmost image would be the center of the three stones concerned */
                        if (closestX == -20) {
                            telemetry.addData("Skystone Target:", "Center");
                            Where = 1;
                        }

                        //Right most stone of the two
                        if (closestX == 20) {
                            telemetry.addData("Skystone Target:", "Right");
                            Where = 2;
                        }

                        //Also express the relative pose (for info purposes)

                        telemetry.addData("Pos (in)", "{X, Y, Z} = %.1f, %.1f, %.1f",
                                translation.get(0), translation.get(1), translation.get(2));
                    }
                } else {
                    telemetry.addData("Visible Target", "none");
                    Where = 3;
                    LF.setPower(.4);        //strafe right
                    LB.setPower(-.4);
                    RF.setPower(-.4);
                    RB.setPower(.39);
                    sleep(375);

                    LF.setPower(0);     //wait
                    LB.setPower(0);
                    RF.setPower(0);
                    RB.setPower(0);
                    sleep(1000);


                }
                telemetry.update();
                timer.reset();

                if (Where == 1) {
                    telemetry.addLine("Position 1");
                    telemetry.update();

//                LF.setPower(-.4);
//                LB.setPower(.4);
//                RF.setPower(.4);
//                RB.setPower(-.385);
//                sleep(300);
                    LF.setPower(.4);        //move left a bit
                    LB.setPower(-.4);
                    RF.setPower(-.4);
                    RB.setPower(.39);
                    sleep(100);

                    LF.setPower(.25);       //drive forward
                    LB.setPower(.25);
                    RF.setPower(.25);
                    RB.setPower(.25);
                    sleep(400);

                    LF.setPower(0);     //wait a bit
                    LB.setPower(0);
                    RF.setPower(0);
                    RB.setPower(0);
                    sleep(1000);

                    Crane1.setPower(1);     //lower boom
                    sleep(2000);

                    FI.setPower(-.5);       //suck
                    sleep(1000);
                    FI.setPower(0);
                    Crane1.setPower(-1);        //raise boom a tiny bit
                    sleep(250);

                    LF.setPower(-.4);       //back up
                    LB.setPower(-.4);
                    RF.setPower(-.4);
                    RB.setPower(-.4);
                    sleep(750);

                    LF.setPower(-0.5);      //go to other side
                    LB.setPower(0.5);
                    RF.setPower(0.5);
                    RB.setPower(-0.5);
                    sleep(2000);

                    FI.setPower(.5);        //unsuck
                    sleep(750);
                    FI.setPower(0);

                    LF.setPower(0.5);       //go to line
                    LB.setPower(-0.5);
                    RF.setPower(-0.5);
                    RB.setPower(0.5);
                    sleep(2000);


                    LF.setPower(0);     //stop doing everything
                    LB.setPower(0);
                    RF.setPower(0);
                    RB.setPower(0);
                    sleep(100000);


                }
                if (Where == 2) {
                    telemetry.addLine("Position 2");
                    telemetry.update();
//                LF.setPower(-.4);
//                LB.setPower(.4);
//                RF.setPower(.4);
//                RB.setPower(-.385);
//                sleep(300);
                    LF.setPower(.4);        //move left a bit
                    LB.setPower(-.4);
                    RF.setPower(-.4);
                    RB.setPower(.39);
                    sleep(100);

                    LF.setPower(.25);       //drive forward
                    LB.setPower(.25);
                    RF.setPower(.25);
                    RB.setPower(.25);
                    sleep(400);

                    LF.setPower(0);     //wait a bit
                    LB.setPower(0);
                    RF.setPower(0);
                    RB.setPower(0);
                    sleep(1000);

                    Crane1.setPower(1);     //lower boom
                    sleep(2000);

                    FI.setPower(-.5);       //suck
                    sleep(1000);
                    FI.setPower(0);
                    Crane1.setPower(-1);        //raise boom a tiny bit
                    sleep(250);

                    LF.setPower(-.4);       //back up
                    LB.setPower(-.4);
                    RF.setPower(-.4);
                    RB.setPower(-.4);
                    sleep(750);

                    LF.setPower(-0.5);      //go to other side
                    LB.setPower(0.5);
                    RF.setPower(0.5);
                    RB.setPower(-0.5);
                    sleep(2000);

                    FI.setPower(.5);        //unsuck
                    sleep(750);
                    FI.setPower(0);

                    LF.setPower(0.5);       //go to line
                    LB.setPower(-0.5);
                    RF.setPower(-0.5);
                    RB.setPower(0.5);
                    sleep(2000);


                    LF.setPower(0);     //stop doing everything
                    LB.setPower(0);
                    RF.setPower(0);
                    RB.setPower(0);
                    sleep(100000);



                }
                if (Where == 4) {
                    telemetry.addLine("Position 3");
                    telemetry.update();
//                LF.setPower(-.4);
//                LB.setPower(.4);
//                RF.setPower(.4);
//                RB.setPower(-.385);
//                sleep(300);

                    LF.setPower(.4);        //move left a bit
                    LB.setPower(-.4);
                    RF.setPower(-.4);
                    RB.setPower(.39);
                    sleep(100);

                    LF.setPower(.25);       //drive forward
                    LB.setPower(.25);
                    RF.setPower(.25);
                    RB.setPower(.25);
                    sleep(400);

                    LF.setPower(0);     //wait a bit
                    LB.setPower(0);
                    RF.setPower(0);
                    RB.setPower(0);
                    sleep(1000);

                    Crane1.setPower(1);     //lower boom
                    sleep(2000);

                    FI.setPower(-.5);       //suck
                    sleep(1000);
                    FI.setPower(0);
                    Crane1.setPower(-1);        //raise boom a tiny bit
                    sleep(250);

                    LF.setPower(-.4);       //back up
                    LB.setPower(-.4);
                    RF.setPower(-.4);
                    RB.setPower(-.4);
                    sleep(750);

                    LF.setPower(-0.5);      //go to other side
                    LB.setPower(0.5);
                    RF.setPower(0.5);
                    RB.setPower(-0.5);
                    sleep(2000);

                    FI.setPower(.5);        //unsuck
                    sleep(750);
                    FI.setPower(0);

                    LF.setPower(0.5);       //go to line
                    LB.setPower(-0.5);
                    RF.setPower(-0.5);
                    RB.setPower(0.5);
                    sleep(2000);


                    LF.setPower(0);     //stop doing everything
                    LB.setPower(0);
                    RF.setPower(0);
                    RB.setPower(0);
                    sleep(100000);
                }
            }

            // Disable Tracking when we are done;
            targetsSkyStone.deactivate();
        }
    }}
