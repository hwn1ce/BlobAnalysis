import marytts.util.string.StringUtils;

import org.opencv.core.*;
import org.opencv.highgui.ImageWindow;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.photo.Photo;
import org.opencv.videoio.VideoCapture;
import org.opencv.highgui.HighGui;

import javax.swing.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.opencv.core.CvType.CV_8UC1;

public class ImageOut{
    public static void main(String[] args) throws IOException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        VideoCapture video = new VideoCapture(0); //initialize video feed
        if(!video.isOpened()){
            video.open(0);
        }
        Mat matrix = new Mat(480, 600, CV_8UC1);
        Mat scanMatrix = new Mat(480, 600, CV_8UC1);
        JFileChooser fc = new JFileChooser();
        int approve = fc.showOpenDialog(null);
        char[] dir;
        StringBuilder file = new StringBuilder();
        String ttsout = "";
        String[] tts = {"Top", "Bottom", "Left", "Right"};
        if(approve == JFileChooser.APPROVE_OPTION){
            dir = fc.getSelectedFile().getCanonicalPath().toCharArray();
            for(char c : dir){
                file.append(c);
                if(c == '\\'){
                    file.append("\\");
                }
            }
            /*if (fc.getName() != "cascade (1).xml"){
                System.out.println("Not the right file. File selected was: " + fc.getName() + ". Directory was: " + file);
                file = "C:\\Users\\sthansen0305\\Downloads\\cascade (1).xml";
            }*/
        }

        CascadeClassifier clsfr = new CascadeClassifier(file.toString()); //"C:\\Users\\sthansen0305\\Downloads\\cascade.xml"
        MatOfRect detect = new MatOfRect();
        List<Mat> images = new ArrayList<>();
        //List<Mat> channels = new ArrayList<>();
        images.add(scanMatrix);
        int x = 0;
        while(true){
            video.read(matrix); // take next frame and set it to the matrix
            /*Core.split(matrix, channels);
            for(Mat mat : channels){
                images.set(0, mat);
                Photo.denoise_TVL1(images, scanMatrix);
            }
            Core.merge(channels, matrix);*/
            //if (x < 1) Imgcodecs.imwrite("C:\\Users\\sthansen0305\\OneDrive - Mesa Public Schools\\Pictures\\Pre-process.png", matrix);
            // x+=1;
            Imgproc.cvtColor(matrix, scanMatrix, Imgproc.COLOR_RGB2GRAY);
            images.set(0, scanMatrix);
            Photo.denoise_TVL1(images, scanMatrix);
            Imgproc.resize(matrix, scanMatrix, new Size(300, 300), 0, 0, Imgproc.INTER_AREA);
            //make the matricies variable in size****
            clsfr.detectMultiScale(scanMatrix, detect);
            for (Rect rect : detect.toArray()) {
                Imgproc.rectangle(matrix, new Point(rect.x, rect.y),
                        new Point((rect.x + rect.width), (rect.y + rect.height)),
                        new Scalar(0, 255, 0));
                //ttsout = tts[((rect.y*(matrix.height()/300)+rect.height)/matrix.height())-1];
            }
            // if (x < 2) Imgcodecs.imwrite("C:\\Users\\sthansen0305\\OneDrive - Mesa Public Schools\\Pictures\\Post-process.png", matrix);
            HighGui.imshow("Video", matrix);
            Iterator<Map.Entry<String,
                    ImageWindow>> iter = HighGui.windows.entrySet().iterator();
            StringUtils.toInputStream(ttsout); //run checks for rectangle location and modify Sitring

            while (iter.hasNext()) {
                Map.Entry<String,
                        ImageWindow> entry = iter.next();
                ImageWindow win = entry.getValue();
                if (win.alreadyUsed) {
                    iter.remove();
                    win.frame.dispose();
                }
            }

            // (if) Create (else) Update frame
            for (ImageWindow win : HighGui.windows.values()) {

                if (win.img != null) {

                    ImageIcon icon = new ImageIcon(HighGui.toBufferedImage(win.img));

                    if (win.lbl == null) {
                        JFrame frame = new JFrame("Video");
                        frame.addWindowListener(new java.awt.event.WindowAdapter() {
                            @Override
                            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                                HighGui.n_closed_windows++;
                                if (HighGui.n_closed_windows == HighGui.windows.size()) HighGui.latch.countDown();
                            }
                        });
                        if (win.flag == HighGui.WINDOW_AUTOSIZE) frame.setResizable(false);
                        JLabel lbl = new JLabel(icon);
                        win.setFrameLabelVisible(frame, lbl);
                    } else {
                        win.lbl.setIcon(icon);
                    }
                } else {
                    System.err.println("Error: no imshow associated with" + " namedWindow: \"" + win.name + "\"");
                    System.exit(-1);
                }
            }

            try {
                HighGui.latch.await(10, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Set all windows as already used
            for (ImageWindow win : HighGui.windows.values())
                win.alreadyUsed = true;

        }
    }
}