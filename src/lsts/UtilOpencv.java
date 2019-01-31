package lsts;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;;

public class UtilOpencv {
	/** Enumeration of operating system families. */
    public enum Family {
        /** Microsoft Windows. */
        WINDOWS,
        /** Unix variants. */
        UNIX,
        /** Mac */
        MAC,
        /** Sol */
        SOL,
        /** unknown OS */
        UNKNOWN
    }

	static VideoCapture videoFile;
	static VideoCapture videoUsb;
	static VideoCapture videoIpCam;
	static Mat videoFileFrame;
	static Mat videoUsbFrame;
	static Mat videoIpCamFrame;
	static boolean isVideoFileOpen;
	static boolean isVideoUsbOpen;
	static boolean isVideoIpCamOpen;
	static int fpsVideoFile;
	static int widthVideoFile;
	static int widthVideoUsb;
	static int widthVideoIpCam;
	static int heightVideoFile;
	static int heightVideoUsb;
	static int heightVideoIpCam;
	static String libOpencv;
	static double shiftValues[];
	static int cntImg = 0;
	
	static VideoWriter videoWriter;
	static VideoWriter videoWriterSingle;
	static boolean firstframeConfig = true;
	static int fps = 0;
	static int fpsSingle = 0;
	static String path;
	static String pathSingle;
	
	static ImShow showDebug = new ImShow("debug");

	public static void VideoInit(String pathFolder, int fpsVideo) {
		firstframeConfig = true;
		fps = fpsVideo;
		
		File directory = new File(pathFolder);
	    if (! directory.exists()){
	        directory.mkdir();
	        //System.out.println("create folder");
	    }
	    
		Date date = new Date();
		String dateFolder = String.format("%tT", date).replace(":", "-");
		path = pathFolder+File.separator+dateFolder+".avi";
		pathSingle = pathFolder+File.separator+dateFolder+"-single.avi";
	}
		
	public static String GetVideoSavedPath() {
		return path;
	}
	
	public static void AddVideoFrame(Mat frame, Mat singleFrame) {
		if (firstframeConfig) {
			videoWriter = new VideoWriter(path, VideoWriter.fourcc('M', 'J', 'P', 'G'), fps, frame.size());
			if(singleFrame != null)
				videoWriterSingle = new VideoWriter(pathSingle, VideoWriter.fourcc('M', 'J', 'P', 'G'), fps, singleFrame.size());
			firstframeConfig = false;
		}
		
		if (videoWriter.isOpened() == false) {
			videoWriter.release();
			if(singleFrame != null)
				videoWriterSingle.release();
			throw new IllegalArgumentException("Video Writer Exception: VideoWriter not opened," + "check parameters.");

		} else {
			//System.out.println("save frame");
			//showDebug.setVisibleWindow(true);
			//showDebug.showImage(frame);
			// Write video
			videoWriter.write(frame);
			if(singleFrame != null)
				videoWriterSingle.write(singleFrame);
		}
	}
	
	public static void CloseVideoWrite() {
		try {
			videoWriter.release();
			if(videoWriterSingle.isOpened())
				videoWriterSingle.release();
			firstframeConfig = true;
		}catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public static boolean IsVideoWriterOpen() {
		return videoWriter.isOpened();
	}

	public static Family getOS(){
    	String OS = System.getProperty("os.name").toLowerCase();
    	if (OS.indexOf("win") >= 0) {
            return Family.WINDOWS;
        }
    	else if (OS.indexOf("mac") >= 0) {
            return Family.MAC;
        }
    	else if (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 ) {
            return Family.UNIX;
        }
    	else if (OS.indexOf("sunos") >= 0) {
            return Family.SOL;
        }
    	else {
            return Family.UNKNOWN;
        }
    }

	public static int getArch(){
    	String arch = System.getProperty("os.arch").toLowerCase();
    	int archResult = 0;
    	if (arch.contains("x86-64") || arch.contains("amd64") || arch.contains("em64t")
                || arch.contains("x86_64")) {
    		archResult = 64;
        }
        else if (arch.equals("x86") || arch.contains("pentium") || arch.contains("i386")
                || arch.contains("i486") || arch.contains("i586") || arch.contains("i686")) {
        	archResult = 32;
        }
        else {
            throw new RuntimeException("Unknown architecture");
        }
    	return archResult;
    }

	/** Convert a Mat image to bufferedImage */
	public static BufferedImage matToBufferedImage(Mat matrix) {
		try{
			if (!matrix.empty()){
				int cols = matrix.cols();
				int rows = matrix.rows();
				int elemSize = (int) matrix.elemSize();
				byte[] data = new byte[cols * rows * elemSize];
				int type;
				matrix.get(0, 0, data);
				switch (matrix.channels()) {
				case 1:
					type = BufferedImage.TYPE_BYTE_GRAY;
					break;
				case 3:
					type = BufferedImage.TYPE_3BYTE_BGR;
					// bgr to rgb
					byte b;
					for (int i = 0; i < data.length; i = i + 3) {
						b = data[i];
						data[i] = data[i + 2];
						data[i + 2] = b;
					}
					break;
				default:
					mainLoader.printToconsole("ERROR: CHANNEL ("+matrix.channels()+")");
					return null;
				}
				BufferedImage image2 = new BufferedImage(cols, rows, type);
				image2.getRaster().setDataElements(0, 0, cols, rows, data);
				return image2;
			}
			else {
				mainLoader.printToconsole("ERROR: Empty image in mattobuff.");
				return null;
			}
		}catch(Exception e){
			//e.printStackTrace();
			//mainLoader.printToconsole("ERROR: Empty image in mattobuff.");
			return null;
		}
	}

	/** Convert bufferedImage to Mat */
	public static Mat bufferedImageToMat(BufferedImage in) {
		Mat out;
		if (in.getType() == BufferedImage.TYPE_3BYTE_BGR) {
			out = new Mat(in.getHeight(), in.getWidth(), CvType.CV_8UC3);
			byte[] pixels = ((DataBufferByte) in.getRaster().getDataBuffer()).getData();
			out.put(0, 0, pixels);
		} else if (in.getType() == BufferedImage.TYPE_INT_RGB) {
			out = new Mat(in.getHeight(), in.getWidth(), CvType.CV_8UC3);
			byte[] data = new byte[in.getWidth() * in.getHeight() * (int) out.elemSize()];
			int[] dataBuff = in.getRGB(0, 0, in.getWidth(), in.getHeight(), null, 0, in.getWidth());
			for (int i = 0; i < dataBuff.length; i++) {
				data[i * 3] = (byte) ((dataBuff[i] >> 16) & 0xFF);
				data[i * 3 + 1] = (byte) ((dataBuff[i] >> 8) & 0xFF);
				data[i * 3 + 2] = (byte) ((dataBuff[i] >> 0) & 0xFF);
			}
			out.put(0, 0, data);
		} else {
			out = new Mat(in.getHeight(), in.getWidth(), CvType.CV_8UC1);
			byte[] pixels = ((DataBufferByte) in.getRaster().getDataBuffer()).getData();
			out.put(0, 0, pixels);
		}
		return out;
	}
	
	public static Mat joinMatImage(Mat left, Mat right) {
		Mat dst = new Mat();
		List<Mat> src = Arrays.asList(left, right);
		Core.hconcat(src, dst);
		return dst;
	}
	
	public static BufferedImage joinBufferedImage(BufferedImage img1, BufferedImage img2) {
		    int offset = 2;
		    int width = img1.getWidth() + img2.getWidth() + offset;
		    int height = Math.max(img1.getHeight(), img2.getHeight()) + offset;
		    BufferedImage newImage = new BufferedImage(width, height,
		        BufferedImage.TYPE_INT_ARGB);
		    Graphics2D g2 = newImage.createGraphics();
		    Color oldColor = g2.getColor();
		    g2.setPaint(Color.BLACK);
		    g2.fillRect(0, 0, width, height);
		    g2.setColor(oldColor);
		    g2.drawImage(img1, null, 0, 0);
		    g2.drawImage(img2, null, img1.getWidth() + offset, 0);
		    g2.dispose();
		    return newImage;
	}

	/** Convert color image */
	public static Mat convertImgColor( Mat img, boolean toColor ){
		Mat result = new Mat();

		if (!toColor)
			Imgproc.cvtColor(img, result, Imgproc.COLOR_BGR2GRAY);
		else
			Imgproc.cvtColor(img, result, Imgproc.COLOR_GRAY2BGR);

		return result;
	}

	/** Resize Buffered Image */
	public static BufferedImage resize(BufferedImage img, int newW, int newH) {
		Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_DEFAULT);
		BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_3BYTE_BGR);

		Graphics2D g2d = dimg.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();

		return dimg;
	}
	
	/** Resize Mat Image */
	public static Mat resizeMat(Mat img, int newW, int newH) {
		Mat temp = new Mat(newH, newW, img.type());
		Imgproc.resize(img, temp, new Size(newH, newW));
		return temp;
	}

	/** Add text to Buffered Image */
	public static BufferedImage addText(BufferedImage old, String text, Color m_color, int posX, int posY) {
		BufferedImage img = new BufferedImage(old.getWidth(), old.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g1d = img.createGraphics();
		g1d.drawImage(old, 0, 0, null);
		g1d.setPaint(Color.DARK_GRAY);
		g1d.setFont(new Font("Serif", Font.BOLD, 14));
		FontMetrics fm = g1d.getFontMetrics();
		g1d.drawString(text, posX - fm.stringWidth(text) - 10, posY);

		g1d.setPaint(m_color);
		g1d.setFont(new Font("Serif", Font.BOLD, 14));
		fm = g1d.getFontMetrics();
		g1d.drawString(text, posX - fm.stringWidth(text) - 12, posY);
		g1d.dispose();
		return img;
	}

	/** Save a snapshot to disk */
	public static void saveSnapshot(BufferedImage image, String snapshotdir) {
		Date date = new Date();
		String dateFolder = String.format("%tT", date);
		String imageJpeg = String.format("%s/%s.png", snapshotdir, dateFolder.replace(":", "-"));
		File outputfile = new File(imageJpeg);
		try {
			File pDir = outputfile.getParentFile();
			if (!pDir.exists())
				pDir.mkdirs();
			ImageIO.write(image, "png", outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** Load a Image */
	public static BufferedImage loadImage (String pathToImage){
		if (getOS() == Family.UNIX)
			pathToImage = pathToImage.replaceFirst("file:", "");
		else if (getOS() == Family.WINDOWS)
			pathToImage = pathToImage.replaceFirst("file:/", "");
		
		//mainLoader.printToconsole(pathToImage.toString());
		Mat image = Imgcodecs.imread(pathToImage.toString(), Imgcodecs.IMREAD_ANYCOLOR);
		return matToBufferedImage(image);
	}

	/** Load a Image */
	public static Mat loadImageMat (String pathToImage){
		if (getOS() == Family.UNIX)
			pathToImage = pathToImage.replaceFirst("file:", "");
		else if (getOS() == Family.WINDOWS)
			pathToImage = pathToImage.replaceFirst("file:/", "");

		Mat image = Imgcodecs.imread(pathToImage.toString(), Imgcodecs.IMREAD_ANYCOLOR);
		return image;
	}

	/** Open a video file */
	public static Mat openVideoFile (String pathToImage){
		if (getOS() == Family.UNIX)
			pathToImage = pathToImage.replaceFirst("file:", "");
		else if (getOS() == Family.WINDOWS)
			pathToImage = pathToImage.replaceFirst("file:/", "");

		isVideoFileOpen = false;
		videoFile = new VideoCapture(pathToImage.toString());
		videoFileFrame = new Mat();
		if (videoFile.read(videoFileFrame)){
			isVideoFileOpen = true;
			///http://stackoverflow.com/questions/21066875/opencv-constants-captureproperty
			/*
			 * CAP_PROP_POS_MSEC =0,
			 * CAP_PROP_POS_FRAMES =1,
			 * CAP_PROP_POS_AVI_RATIO =2,
			 * CAP_PROP_FRAME_WIDTH =3,
			 * CAP_PROP_FRAME_HEIGHT =4,
			 * CAP_PROP_FPS =5,
			 * CAP_PROP_FOURCC =6,
			 * CAP_PROP_FRAME_COUNT =7,
			 * CAP_PROP_FORMAT =8,
			 * CAP_PROP_MODE =9,
			 * CAP_PROP_BRIGHTNESS =10,
			 * CAP_PROP_CONTRAST =11,
			 * CAP_PROP_SATURATION =12,
			 * CAP_PROP_HUE =13,
			 * CAP_PROP_GAIN =14,
			 * CAP_PROP_EXPOSURE =15,
			 * CAP_PROP_CONVERT_RGB =16,
			 * CAP_PROP_WHITE_BALANCE_BLUE_U =17,
			 * CAP_PROP_RECTIFICATION =18,
			 * CAP_PROP_MONOCROME =19,
			 * CAP_PROP_SHARPNESS =20,
			 * CAP_PROP_AUTO_EXPOSURE =21,
			 * // DC1394: exposure control done by camera, user can adjust refernce level using this feature
			 * CAP_PROP_GAMMA =22,
			 * CAP_PROP_TEMPERATURE =23,
			 * CAP_PROP_TRIGGER =24,
			 * CAP_PROP_TRIGGER_DELAY =25,
			 * CAP_PROP_WHITE_BALANCE_RED_V =26,
			 * CAP_PROP_ZOOM =27,
			 * CAP_PROP_FOCUS =28,
			 * CAP_PROP_GUID =29,
			 * CAP_PROP_ISO_SPEED =30,
			 * CAP_PROP_BACKLIGHT =32,
			 * CAP_PROP_PAN =33,
			 * CAP_PROP_TILT =34,
			 * CAP_PROP_ROLL =35,
			 * CAP_PROP_IRIS =36,
			 * CAP_PROP_SETTINGS =37
			 */
			fpsVideoFile = (int)videoFile.get(5);
			widthVideoFile = (int)videoFile.get(3);
			heightVideoFile = (int)videoFile.get(4);

			return videoFileFrame;
		}
		else{
			isVideoFileOpen = true;
			return null;
		}
	}

	/** Close a video file input */
	public static void closeVideoFile(){
		videoFile.release();
		isVideoFileOpen = false;
	}

	/** Is video file open */
	public static boolean isVideoFileOpen (){
		return isVideoFileOpen;
	}

	/** Info Video file fps */
	public static int getFpsVideoFile(){
		return fpsVideoFile;
	}

	public static int getWidthVideoFile(){
		return widthVideoFile;
	}

	public static int getHeightVideoFile(){
		return heightVideoFile;
	}

	public static int getFrameCountVideoFile(){
		return (int)videoFile.get(7);
	}
	/** Grab video file frame */
	public static Mat grabFrameVideoFile(){
		videoFileFrame = new Mat();
		try{
			videoFile.read(videoFileFrame);
			if (!videoFileFrame.empty()){
				isVideoFileOpen = true;
				return videoFileFrame;
			}
			else{
				isVideoFileOpen = false;
				return null;
			}
		}
		catch(Exception e){
			mainLoader.printToconsole("ERRO grab: "+e.getMessage());
			isVideoFileOpen = false;
			return null;
		}
	}

	public static double getFramePosition(){
		return videoFile.get(1);
	}

	public static boolean jumpFramesBackward( int numberFrames ){
		boolean result = false;
		double cnt = videoFile.get(1);
		double frameBack = cnt - numberFrames + 1;
		if (frameBack > 0){
			result = true;
			videoFile.set(1, frameBack);
		}
		else{
			result = false;
		}
		return result;
	}

	public static boolean jumpFramesForward( int numberFrames ){
		int cnt = 0;
		boolean result = false;
		boolean jumpWhile = false;
		while(cnt < numberFrames - 1 && !jumpWhile){
			if (grabFrameVideoFile() != null )
				result = true;
			else{
				result = false;
				jumpWhile = true;
			}

			cnt++;
		}
		return result;
	}

	public static Mat getFrameFileCounter(){
		if (isVideoFileOpen){
			return grabFrameVideoFile();
		}
		else{
			return null;
		}

	}

	/** Open a usb video */
	public static Mat openVideoUsb (int idCam){
		isVideoUsbOpen = false;
		videoUsb = new VideoCapture(idCam);
		videoUsbFrame = new Mat();
		if (videoUsb.read(videoUsbFrame)){
			isVideoUsbOpen = true;
			widthVideoUsb = (int)videoUsb.get(3);
			heightVideoUsb = (int)videoUsb.get(4);

			return videoUsbFrame;
		}
		else{
			isVideoUsbOpen = false;
			return null;
		}
	}

	/** Close a video usb input */
	public static void closeVideoUsb(){
		videoUsb.release();
		isVideoUsbOpen = false;
	}

	/** Is video usb open */
	public static boolean isVideoUsbOpen (){
		return isVideoUsbOpen;
	}

	public static int getWidthVideoUsb(){
		return widthVideoUsb;
	}

	public static int getHeightVideoUsb(){
		return heightVideoUsb;
	}

	/** Grab frame */
	public static Mat grabFrameVideoUsb(){
		videoUsbFrame = new Mat();
		try{
			videoUsb.read(videoUsbFrame);
			if (!videoUsbFrame.empty()){
				isVideoUsbOpen = true;
				return videoUsbFrame;
			}
			else{
				isVideoUsbOpen = false;
				return null;
			}
		}
		catch(Exception e){
			mainLoader.printToconsole("ERRO grab: "+e.getMessage());
			isVideoUsbOpen = false;
			return null;
		}
	}

	/** Open a ipcam video */
	public static boolean openVideoIpCam(String urlHost){
		videoIpCam = new VideoCapture();
		try{
			videoIpCam.open(urlHost);
			if (videoIpCam.isOpened()) {
				isVideoIpCamOpen = true;
				widthVideoIpCam = (int)videoIpCam.get(3);
				heightVideoIpCam = (int)videoIpCam.get(4);
				return true;
			}
			else{
				mainLoader.printToconsole("Cannot open IpCam (wrong url???)");
				isVideoIpCamOpen = false;
				return false;
			}
		}
		catch(Exception e){
			e.printStackTrace();
			mainLoader.printToconsole("Cannot open IpCam");
			isVideoIpCamOpen = false;
			return false;
		}
	}

	/** Close a video ipca input */
	public static void closeVideoIpCam(){
		videoIpCam.release();
		isVideoIpCamOpen = false;
	}

	/** Is video IpCam open */
	public static boolean isVideoIpCamOpen (){
		return isVideoIpCamOpen;
	}

	public static int getWidthVideoIpCam(){
		return widthVideoIpCam;
	}

	public static int getHeightVideoIpCam(){
		return heightVideoIpCam;
	}

	/** Grab frame */
	public static Mat grabFrameVideoIpCam(){
		videoIpCamFrame = new Mat();
		try{

			videoIpCam.read(videoIpCamFrame);

			if (!videoIpCamFrame.empty()){
				isVideoIpCamOpen = true;
				return videoIpCamFrame;
			}
			else{
				isVideoIpCamOpen = false;
				return null;
			}
		}
		catch(Exception e){
			mainLoader.printToconsole("ERRO grab: "+e.getMessage());
			isVideoIpCamOpen = false;
			return null;
		}
	}

	//!Find OPENCV JNI in host PC
	public static boolean findOpenCV(String pathJava, String pathLib) {
		boolean result = false;
		if (getOS() == Family.UNIX) {
			File path = new File(pathJava);
			String[] children = !path.exists() ? new String[0] : path.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					boolean ret = name.toLowerCase().startsWith("libopencv_java");
					ret = ret && name.toLowerCase().endsWith(".so");
					return ret;
				}
			});
			
			path = new File(pathLib);
			String[] children2 = !path.exists() ? new String[0] : path.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					boolean ret = name.toLowerCase().startsWith("libopencv_");
					ret = ret && name.toLowerCase().endsWith(".so");
					return ret;
				}
			});
			
			if (children.length > 0 && children2.length > 0) {
				String filenameJava = children[0];
				//String filenameLib = children2[0];
				//libOpencv = filename.toString().replaceAll("lib", "").replaceAll(".so", "");
				//System.out.println(filenameJava);
				//System.out.println(filenameLib);
				//System.out.println(System.getProperty("java.library.path"));
				//System.loadLibrary(libOpencv);
				try {
					//System.out.println(libOpencv);
					//System.out.println(Core.NATIVE_LIBRARY_NAME);
					//System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
					//System.loadLibrary("opencv_core");
					//System.loadLibrary("opencv_java401");
					System.load(pathJava+File.separator+filenameJava);
					System.load(pathLib+File.separator+"libopencv_core.so");
					return true;
				} catch (Exception e) {
					mainLoader.printToconsole("Opencv not found in "+ pathJava + " | " + pathLib + e.getMessage());
					return false;
				} catch (Error e) {
					mainLoader.printToconsole("Opencv not found in "+ pathJava + " | " + pathLib + e.getMessage());
					return false;
				}
			}
		} else {
			// If we are here is not loaded yet
			try {
				libOpencv = "opencv_java2411";
				System.loadLibrary("opencv_java2411");
				System.loadLibrary("libopencv_core2411");
				System.loadLibrary("libopencv_highgui2411");
				try {
					System.loadLibrary("opencv_ffmpeg2411" + (getArch() == 64 ? "_64" : ""));
				} catch (Exception e1) {
					System.loadLibrary("opencv_ffmpeg2411");
				} catch (Error e1) {
					System.loadLibrary("opencv_ffmpeg2411");
				}
				result = true;
			} catch (Exception e) {
				result = false;
				mainLoader.printToconsole("Opencv not found - " + e.getMessage());
			} catch (Error e) {
				result = false;
				mainLoader.printToconsole("Opencv not found - " + e.getMessage());
			}
		}
		//if (!result)
		//	mainLoader.printToconsole("Opencv not found - please install OpenCv 4.0.1 and dependencies.");

		return result;
	}

    public static String getVersionLib(){
    	String versionPath = libOpencv;
    	String[] parts = versionPath.split("java");
    	return parts[1].replaceFirst("24", "2.4.");
    }

	public static void cloneBufferedImage(BufferedImage buff1, BufferedImage buff2, float opaque, int x, int y) {
		Graphics2D g2d = buff1.createGraphics();
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opaque));
		g2d.drawImage(buff2, x, y, null);
		g2d.dispose();
	}

	public static BufferedImage medianBlur(BufferedImage src, int value){
		if (src == null)
			return null;

		Mat dst = new Mat();
		Imgproc.medianBlur(bufferedImageToMat(src), dst, value);
		return matToBufferedImage(dst);
	}

	public static BufferedImage paintSolidColorBufferedImage(int cols, int rows){
		BufferedImage solidColor = new BufferedImage(cols, rows, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D graphics = solidColor.createGraphics();
		graphics.setColor(Color.BLACK);
		graphics.fillRect ( 0, 0, solidColor.getWidth(), solidColor.getHeight() );
		graphics.dispose();

		return solidColor;
	}

	public static Mat resizeToHalf(Mat src){
		return bufferedImageToMat(resize(matToBufferedImage(src), src.width()/2, src.height()/2));
	}
	

	/**
	 *Rotate image by 90, 180, 270, -90, -180, -270
	 *
	 *@param src : input image
	 *@param angle : 90, 180, 270, -90, -180, -270
	 *@param return : image rotated
	 */
	public static Mat rotateImage(Mat src, int angle){
		Mat dstR = new Mat();

		if (angle == 270 || angle == -90) {
			// Rotate clockwise 270 degrees
			Core.transpose(src, dstR);
			Core.flip(dstR, dstR, 0);
		}
		else if (angle == 180 || angle == -180) {
			// Rotate clockwise 180 degrees
			Core.flip(src, dstR, -1);
		}
		else if (angle == 90 || angle == -270) {
			// Rotate clockwise 90 degrees
			Core.transpose(src, dstR);
			Core.flip(dstR, dstR, 1);
		}
		else if (angle == 360 || angle == 0 || angle == -360) {
			src.copyTo(dstR);
		}
		else {
			mainLoader.printToconsole("Wrong input angle for rotation");
			src.copyTo(dstR);
		}

		return dstR;
	}
}