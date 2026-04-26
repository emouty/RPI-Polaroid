package polaroid;

import java.io.File;
import java.io.IOException;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.gpio.digital.PullResistance;
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
        final Context pi4j = Pi4J.newAutoContext();
        DigitalInput myButton = pi4j.create(DigitalInput.newConfigBuilder(pi4j)
                .id("my-button")
                .name("MyButton").bcm(20)
                .pull(PullResistance.PULL_UP)
                .build());

        // installLibrary("");
        installTempLibrary();

        CameraHandler cameraHandler = new CameraHandler();
        Camera camera = cameraHandler.getCamera();
        camera.takePicture(new FilePictureCaptureHandler(new File("picam-2.jpg")));

        while (true) {
            if (myButton.state() == DigitalState.LOW && !shutdownTimerStarted) {
                startShutdownTimer = System.currentTimeMillis();
                shutdownTimerStarted = true;
            }
            if (myButton.isHigh() && shutdownTimerStarted
                    && System.currentTimeMillis() - startShutdownTimer > 5000) {
                System.out.println("Shutting Down");
                try {
                    shutdownPi();
                } catch (IOException ex) {
                }

                pi4j.shutdown();
                System.exit(0);

            } else {
                shutdownTimerStarted = false;
            }
        }
    }

    public static void shutdownPi() throws IOException {
        // Prefer ProcessBuilder over deprecated Runtime.exec usage.
        new ProcessBuilder("sudo", "shutdown", "-h", "now").start();
    }
}

// https://github.com/Hopding/JRPiCam
// https://github.com/caprica/picam
