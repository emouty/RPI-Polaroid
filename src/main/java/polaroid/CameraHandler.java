package polaroid;

import uk.co.caprica.picam.Camera;
import uk.co.caprica.picam.CameraConfiguration;
import uk.co.caprica.picam.CameraException;
import uk.co.caprica.picam.enums.Encoding;

public class CameraHandler {
    private CameraConfiguration config;
    private Camera camera;



    public CameraHandler() throws CameraException {
        config = CameraConfiguration.cameraConfiguration()
                .width(1920)
                .height(1080)
                .encoding(Encoding.JPEG)
                .quality(85);
        camera = new Camera(config);
        Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownRunnable()));
    }

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }



    private class ShutdownRunnable implements Runnable {

        public void run() {
            camera.close();
        }
    }
}
