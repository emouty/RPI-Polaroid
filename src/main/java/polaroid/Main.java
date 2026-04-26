package polaroid;

import java.io.File;
import java.io.IOException;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import uk.co.caprica.picam.Camera;
import uk.co.caprica.picam.CameraException;
import uk.co.caprica.picam.CaptureFailedException;
import uk.co.caprica.picam.FilePictureCaptureHandler;
import uk.co.caprica.picam.NativeLibraryException;

import static uk.co.caprica.picam.PicamNativeLibrary.installTempLibrary;

public class Main {
    // this is the timer variable for the delay between blobs
    private long startTime_shot = 0;
    // just so we can wipe out the changes from the first frame
    private boolean firstFrame = true;
    private static long startShutdownTimer;
    private static boolean shutdownTimerStarted = false;



    public static void main(String[] args) throws NativeLibraryException, CameraException, CaptureFailedException {
        // create gpio controller instance
        final GpioController gpio = GpioFactory.getInstance();
        GpioPinDigitalInput myButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_20,
                "MyButton", PinPullResistance.PULL_UP);

        // installLibrary("");
        installTempLibrary();

        CameraHandler cameraHandler = new CameraHandler();
        Camera camera = cameraHandler.getCamera();
        camera.takePicture(new FilePictureCaptureHandler(new File("picam-2.jpg")));


        while (true) {
            if (myButton.isState(PinState.LOW) && !shutdownTimerStarted) {
                startShutdownTimer = System.currentTimeMillis();
                shutdownTimerStarted = true;
            }
            if (myButton.isHigh() && shutdownTimerStarted
                    && System.currentTimeMillis() - startShutdownTimer > 5000) {
                System.out.println("Shutting Down");
                try {
                    shutdownPi();
                } catch (IOException ex) {}

                System.exit(0);

            } else {
                shutdownTimerStarted = false;
            }


        }
    }

    public static void shutdownPi() throws IOException {

        Runtime.getRuntime().exec("sudo shutdown -h now");
    }
}

// https://github.com/Hopding/JRPiCam
// https://github.com/caprica/picam