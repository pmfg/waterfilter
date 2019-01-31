package lsts;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import net.miginfocom.swing.MigLayout;

public class mainLoader extends JFrame implements ActionListener{
	
	private static final long serialVersionUID = 1L;
	static JFrame consoleOutput;
	static JScrollPane consoleScroll;
	static JTextArea printTextOut = null;
	int mainFrameWidth = 480*2;
	int mainFrameHeight = 480;
	int widhtFrame = mainFrameWidth;
	int heightFrame = mainFrameHeight - 20;
	boolean fullScreenPanel1;
	boolean fullScreenPanel2;
	JFrame fullScreenPanel;
	JPanel mainPanel;
	JPanel panel1;
	JPanel panel2;
	JLabel imageTemp1;
	JLabel imageTemp2;
	BufferedImage imageBack = null;
	BufferedImage imageBackFilter = null;
	BufferedImage mainImage = null;
	JMenuBar menuBar;
	JMenu menuOptions;
	JMenuItem menuItemImage;
	JFileChooser fileChooser;
	ImagePreviewPanel preview;
	Thread updaterThreadImage = null;
	boolean isFotoFilter;
	JFrame debugFrame;
	JMenuItem menuItemVideo;
	JButton playButton;
	JButton stopButton;
	JButton pauseButton;
	JButton forwardButton;
	JButton backwardButton;
	boolean isPlay;
	boolean isStop;
	boolean isPause;
	boolean isForward;
	boolean isBackward;
	int stepVideo[] = {2, 4, 6, 8, 10};
	int stepCountForward;
	int stepCountBackward;
	int frameCountVideo;
	JPanel debugPanel;
	JProgressBar pbar;
	static final int MY_MINIMUM = 0;
	JLabel jlabelInfoVideo;
	ImShow showFusion = new ImShow("fusion");
	ImShow showOrignal = new ImShow("original");
	ImShow showRemoveBackScatter = new ImShow("rmScatter");
	BufferedImage bufImage;
	Mat fusion;
	Mat imageFrameVideo;
	BufferedImage original;
	BufferedImage filter;
	Mat originalMat;
	Mat filterMat;
		
	@SuppressWarnings("unused")
	private static final int blkSize = 10 * 10;
	@SuppressWarnings("unused")
    private static final int patchSize = 8;
	@SuppressWarnings("unused")
    private static final double lambda = 10;
	@SuppressWarnings("unused")
    private static final double gamma = 1.7;
	@SuppressWarnings("unused")
    private static final int r = 10;
	@SuppressWarnings("unused")
    private static final double eps = 1e-6;
    private static final int level = 5;

	public static void main(String args[]) {
		initConsoleFrame();
		printToconsole("Welcome");
		printToconsole("Operating System: "+UtilOpencv.getOS()+ " - x"+UtilOpencv.getArch());
		
		boolean state = UtilOpencv.findOpenCV("/usr/share/java/opencv4", "/usr/local/lib");
		if(state)
			printToconsole("Opencv java/lib: /usr/share/java/opencv4 | /usr/local/lib");
		else {
			state = UtilOpencv.findOpenCV("/usr/share/java/opencv4", "/usr/lib");
			if(state)
				printToconsole("Opencv java/lib: /usr/share/java/opencv4 | /usr/lib");
		}
		
		if(!state){
			System.out.println(state);
			printToconsole("No OpenCv found - please install OpenCv 4.0.1 and dependencies.");
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.exit(0);
		}
		
		printToconsole("Version of OpenCV (jar) : " + Core.VERSION);
		new mainLoader();
	}
	
	static void initConsoleFrame(){
		// * JFrame for debug
		consoleOutput = new JFrame("Console Output");
		consoleOutput.setSize(450, 254);
		consoleOutput.setBackground(Color.DARK_GRAY);
		consoleOutput.setResizable(false);
		consoleOutput.setLocation(100, 400);

		JPanel consolePanel = new JPanel();
		//consolePanel.setLayout(new BoxLayout(debugPanel, BoxLayout.Y_AXIS));
		consolePanel.setBorder(BorderFactory.createTitledBorder("Otuput:"));
		
		JButton clearText = new JButton("Clear console");
		clearText.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				printTextOut.setText(null);
			}
		});
		
		printTextOut = new JTextArea(10, 37);
		printTextOut.setEditable(false);
		printTextOut.setLineWrap(true);
		printTextOut.setWrapStyleWord(true);
		
		consoleScroll = new JScrollPane(printTextOut, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		consolePanel.add(consoleScroll);
		consolePanel.add(clearText);

		consoleOutput.add(consolePanel);
		consoleOutput.setVisible(true);
	}
	
	static void printToconsole( String text ){
		Calendar nowHour = Calendar.getInstance();
		DecimalFormat mFormat= new DecimalFormat("00");
		printTextOut.append(mFormat.format(nowHour.get(Calendar.HOUR_OF_DAY))+":"+mFormat.format(nowHour.get(Calendar.MINUTE))
		+":"+mFormat.format(nowHour.get(Calendar.SECOND))+" >"+text+"\n");
		printTextOut.setCaretPosition(printTextOut.getDocument().getLength());
	}
	
	public mainLoader()
    {
        createAndShowMainGUI();
    }

	private void createAndShowMainGUI() {
		setTitle("Main Frame");
		setLocation(150, 20);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setResizable(true);
        setLocation(600, 200);
        setLayout(new MigLayout());
        
        showOrignal.setVisibleWindow(false);
        showFusion.setVisibleWindow(false);
        showRemoveBackScatter.setVisibleWindow(false);
        
        isFotoFilter = false;
        setFrame();
        setMenu();
        paintSolidColor();
        
        updaterThreadImage = updaterThread();
        updaterThreadImage.start();
        
		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e) {
				int i = JOptionPane.showConfirmDialog(null ,"Are you sure you want to quit?", "",JOptionPane.YES_NO_OPTION);
				if (i == JOptionPane.YES_OPTION) {
					System.exit(0);
				}
				else {
					return;
				}
			}
		});
		
		addComponentListener(new ComponentAdapter() {  
    		public void componentResized(ComponentEvent evt) {
    			Component c = evt.getComponent();
    		    widhtFrame = c.getSize().width - 8;
    		    heightFrame = c.getSize().height - 60;
    		    //if (isFotoFilter)
    		    if (UtilOpencv.isVideoFileOpen())
    		    	paintBufferedImage(null, null, true);
    		    else
    		    	paintBufferedImage(null, null, false);
    		}
    	});
	}
	
	private void paintSolidColor(){
		BufferedImage solidColor = new BufferedImage(350, 350, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D graphics = solidColor.createGraphics();
		graphics.setColor(Color.BLACK);
		graphics.fillRect ( 0, 0, solidColor.getWidth(), solidColor.getHeight() );
		graphics.dispose();
		paintBufferedImage(solidColor, null, false);
	}

	private void setFrame(){
		fullScreenPanel1 = false;
		fullScreenPanel2 = false;
		fullScreenPanel = new JFrame();
		fullScreenPanel.setExtendedState(JFrame.MAXIMIZED_BOTH); 
		fullScreenPanel.setUndecorated(true);
		fullScreenPanel.addMouseListener(new MouseAdapter(){
		    @Override
		    public void mouseClicked(MouseEvent e){
		        if(e.getClickCount() == 2 && !UtilOpencv.isVideoFileOpen()){
		            fullScreenPanel.setVisible(false);
		            fullScreenPanel1 = false;
		        	fullScreenPanel2 = false;
		        }
		    }
		});
		fullScreenPanel.setLayout(new BorderLayout());
		
		mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.LINE_AXIS));
        mainPanel.setSize(mainFrameWidth, mainFrameHeight);

		panel1 = new JPanel();
		panel1.setBorder(BorderFactory.createTitledBorder("Original"));
		panel1.setPreferredSize(new Dimension(mainFrameWidth/2 , mainFrameHeight));
		panel1.addMouseListener(new MouseAdapter(){
		    @Override
		    public void mouseClicked(MouseEvent e){
		        if(e.getClickCount() == 2 && !UtilOpencv.isVideoFileOpen()){
		        	fullScreenPanel1 = true;
		        	fullScreenPanel2 = false;
		        	fullScreenPanel.setVisible(true);
		        }
		    }
		});
		mainPanel.add(panel1);
		
		panel2 = new JPanel();
		panel2.setBorder(BorderFactory.createTitledBorder("Filtered"));
		panel2.setPreferredSize(new Dimension(mainFrameWidth/2, mainFrameHeight));
		panel2.addMouseListener(new MouseAdapter(){
		    @Override
		    public void mouseClicked(MouseEvent e){
		        if(e.getClickCount() == 2 && !UtilOpencv.isVideoFileOpen()){
		            fullScreenPanel1 = false;
		        	fullScreenPanel2 = true;
		        	fullScreenPanel.setVisible(true);
		        }
		    }
		});
		mainPanel.add(panel2);
		
		add(mainPanel, "center");
		
		imageTemp1 = new JLabel();
		imageTemp2 = new JLabel();
		pack();
		setVisible(true);
	}
	
	private void paintBufferedImage(BufferedImage image, String path, boolean videoFile){
		if (image == null){
			image = imageBack;
		}
		else{
			imageBack = image;
		}
		
		if(!fullScreenPanel1 && !fullScreenPanel2){						
			if(isFotoFilter) {
				if(path != null) {
					imageBackFilter = FilterImageFusion(path, (widhtFrame/2) - 24, heightFrame - 32);
					mainImage = imageBackFilter;
				}else {
					if(imageBackFilter != null) {
						mainImage = UtilOpencv.resize(imageBackFilter, (widhtFrame/2) - 24, heightFrame - 32);
					}
				}
			}else
				if(videoFile) {
					mainImage = FilterImageFusion(imageBack, (widhtFrame/2) - 24, heightFrame - 32);
				}
				else {
					mainImage = UtilOpencv.resize(imageBack, (widhtFrame/2) - 24, heightFrame - 32);
				}
			
			imageTemp2.setIcon(new ImageIcon(mainImage));
			panel2.add(imageTemp2);
			mainPanel.add(panel2);
			
			mainImage = UtilOpencv.resize(imageBack, (widhtFrame/2) - 24, heightFrame - 32);
			imageTemp1.setIcon(new ImageIcon(mainImage));
			panel1.add(imageTemp1);
			mainPanel.add(panel1);
						
			mainPanel.repaint();
			revalidate();
			repaint();
						
			if(isPlay) {
				//TODO
				UtilOpencv.saveSnapshot(UtilOpencv.joinBufferedImage(original, filter), "Output Images");
				/*System.out.println("type:"+originalMat.type()+" | "+filterMat.type());
				System.out.println("rows:"+originalMat.rows()+" | "+filterMat.rows());
				System.out.println("cols:"+originalMat.cols()+" | "+filterMat.cols());
				System.out.println("dims:"+originalMat.dims()+" | "+filterMat.dims());*/
				
				filterMat = UtilOpencv.resizeMat(filterMat, originalMat.rows(), originalMat.cols());
				
				/*System.out.println("type:"+originalMat.type()+" | "+filterMat.type());
				System.out.println("rows:"+originalMat.rows()+" | "+filterMat.rows());
				System.out.println("cols:"+originalMat.cols()+" | "+filterMat.cols());
				System.out.println("dims:"+originalMat.dims()+" | "+filterMat.dims()+"\n\n");*/
				UtilOpencv.AddVideoFrame(UtilOpencv.joinMatImage(originalMat, filterMat), filterMat);
			}
		}
		else if(fullScreenPanel1 || fullScreenPanel2){
			if (fullScreenPanel2){
				if(isFotoFilter) {
					if(path != null) {
						imageBackFilter = FilterImageFusion(path, fullScreenPanel.getWidth(), fullScreenPanel.getHeight());
						mainImage = imageBackFilter;
					}else {
						if(imageBackFilter != null)
							mainImage = UtilOpencv.resize(imageBackFilter, fullScreenPanel.getWidth(), fullScreenPanel.getHeight());
					}
				}
				else
					if(videoFile)
						mainImage = FilterImageFusion(imageBack, fullScreenPanel.getWidth(), fullScreenPanel.getHeight());
					else
						mainImage = UtilOpencv.resize(imageBack, fullScreenPanel.getWidth(), fullScreenPanel.getHeight());
				
				imageTemp2.setIcon(new ImageIcon(mainImage));
	            fullScreenPanel.add(imageTemp2);
			}else if (fullScreenPanel1){
				mainImage = UtilOpencv.resize(imageBack, fullScreenPanel.getWidth(), fullScreenPanel.getHeight());
				imageTemp1.setIcon(new ImageIcon(mainImage));
	            fullScreenPanel.add(imageTemp1);
			}
			
			fullScreenPanel.revalidate();
            fullScreenPanel.repaint();
		}
	}
	
	private void setMenu() {
		menuBar = new JMenuBar();
		menuOptions = new JMenu("Options");
		menuItemImage = new JMenuItem("Load a Image");
		menuItemImage.addActionListener(this);
		menuOptions.add(menuItemImage);
		menuItemVideo = new JMenuItem("Load a Video");
		menuItemVideo.addActionListener(this);
		menuOptions.add(menuItemVideo);
		menuBar.add(menuOptions);
		setJMenuBar(menuBar);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getActionCommand().toString().equals("Load a Image")){
			filterImage();
		}else if (arg0.getActionCommand().toString().equals("Load a Video")){
			filterVideoFile();
		}
	}

	private void filterImage() {
		try {
			debugFrame.setVisible(false);
		}catch (Exception e) {
			// TODO: handle exception
		}
		//System.out.println("load image");
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Image file", "jpeg", "png", "jpg");
		fileChooser = new JFileChooser();
		fileChooser.setFileFilter(filter);
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")+"/media"));
		fileChooser.setDialogTitle("Image");
		preview = new ImagePreviewPanel();
		fileChooser.setAccessory(preview);
		fileChooser.addPropertyChangeListener(preview);
		
		int result = fileChooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
		    // user selects a file
			printToconsole("Load Image: "+fileChooser.getSelectedFile().getName());
			
			if (fileChooser.getSelectedFile().toURI().toString().indexOf("%20") < 0) {
				isFotoFilter = true;
				paintBufferedImage(UtilOpencv.loadImage(fileChooser.getSelectedFile().toURI().toString()), fileChooser.getSelectedFile().toURI().toString(), false);
			}
			else {
				printToconsole("ERROR: choose image of relative path without space in name.");
			}
		}
	}
		
	@SuppressWarnings("unused")
	private void processAllFilter() {
		imageFrameVideo = UtilOpencv.grabFrameVideoFile();
		//FUSION
		
		fusion = FusionEnhance.enhance(imageFrameVideo, level);
		fusion.convertTo(fusion, CvType.CV_8UC1);
		showFusion.showImage(fusion);
		
		//Remove scatter
		/*fusion = RemoveBackScatter.enhance(imageFrameVideo, blkSize, patchSize, lambda, gamma, r, eps, level);
		fusion.convertTo(fusion, CvType.CV_8UC1);
		showRemoveBackScatter.showImage(fusion);*/
		
		showOrignal.showImage(imageFrameVideo);
	}
	
	private void getVideoFileFrame(){
		Mat frame = UtilOpencv.grabFrameVideoFile();
		original = showFusion.toBufferedImage(frame);
		paintBufferedImage(UtilOpencv.matToBufferedImage(frame), null, true);
		//processAllFilter();
		
		if (isForward){
			frameCountVideo = frameCountVideo + stepVideo[stepCountForward];
			pbar.setValue(frameCountVideo);
		}
		else if (isBackward){
			frameCountVideo = (int) UtilOpencv.getFramePosition();
			pbar.setValue(frameCountVideo);
		}
		else{
			frameCountVideo++;
			pbar.setValue(frameCountVideo);
		}
		
		if (isBackward && (int) UtilOpencv.getFramePosition() < 10){
			isPlay = false;
			isStop = false;
			isPause = true;
			isForward = false;
			isBackward = false;
			printToconsole("Video file -> pause");
			playButton.setEnabled(true);
			stopButton.setEnabled(true);
			pauseButton.setEnabled(false);
			forwardButton.setEnabled(true);
			backwardButton.setEnabled(true);
			stepCountBackward = 0;
			stepCountForward = 0;
			backwardButton.setText("");
			forwardButton.setText("");
		}
	}

	private Thread updaterThread() {
		Thread ret = new Thread("Update Image Thread") {
			@Override
			public void run() {
				while (true) {
					if (isFotoFilter) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						paintBufferedImage(null, null, false);
					}else {
						if (UtilOpencv.isVideoFileOpen()){
	            			if(isPlay){
	            				try{
	                				long startTime = System.currentTimeMillis();
	                				getVideoFileFrame();
	                				long stopTime = System.currentTimeMillis();
	                				long fpsTime = stopTime - startTime;
	                				printToconsole("time used:"+fpsTime+ "ms");
	                				long fpsFinal = 1000 / UtilOpencv.getFpsVideoFile();
	                				while (fpsTime < fpsFinal) {
	                                    stopTime = System.currentTimeMillis();
	                                    fpsTime = stopTime - startTime;
	                				}
	                			}
	                			catch(Exception e){
	                				try{
	                					debugFrame.setVisible(false);
	                					System.out.println("A:"+e.getMessage());
	                				}catch(Exception e1){}
	                				
	                				UtilOpencv.closeVideoFile();
	                			}
	            			}
	            			else if (isForward){
	            				try{
	                				long startTime = System.currentTimeMillis();
	                				UtilOpencv.jumpFramesForward(stepVideo[stepCountForward]);
	                				getVideoFileFrame();
	                				long stopTime = System.currentTimeMillis();
	                				long fpsTime = stopTime - startTime;
	                				long fpsFinal = 1000 / UtilOpencv.getFpsVideoFile();
	                				while (fpsTime < fpsFinal) {
	                                    stopTime = System.currentTimeMillis();
	                                    fpsTime = stopTime - startTime;
	                				}
	                			}
	                			catch(Exception e){
	                				try{
	                					debugFrame.setVisible(false);
	                					System.out.println("B:"+e.getMessage());
	                				}catch(Exception e1){}
	                				
	                				UtilOpencv.closeVideoFile();
	                			}
	            			}
	            			else if (isBackward){
	            				try{
	                				long startTime = System.currentTimeMillis();
	                				UtilOpencv.jumpFramesBackward(stepVideo[stepCountBackward]);
	                				getVideoFileFrame();
	                				long stopTime = System.currentTimeMillis();
	                				long fpsTime = stopTime - startTime;
	                				long fpsFinal = 1000 / UtilOpencv.getFpsVideoFile();
	                				while (fpsTime < fpsFinal) {
	                                    stopTime = System.currentTimeMillis();
	                                    fpsTime = stopTime - startTime;
	                				}
	                			}
	                			catch(Exception e){
	                				try{
	                					debugFrame.setVisible(false);
	                					System.out.println("C:"+e.getMessage());
	                				}catch(Exception e1){}
	                				
	                				UtilOpencv.closeVideoFile();
	                			}
	            			}
	            			else{
	            				try {
									Thread.sleep(1000);
									paintBufferedImage(null, null, true);
								} catch (InterruptedException e) {}
	            			}
	            		}else {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
	            		}
					}
				}
			}
		};
		return ret;
	}
	
	public void filterVideoFile(){
		isFotoFilter = false;
		
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Video file", "avi", "mp4", "mjpeg", "3gp", "mov", "MOV", "wmv", "mkv");
		fileChooser = new JFileChooser();
		fileChooser.setFileFilter(filter);
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
		fileChooser.setDialogTitle("Video");
		preview = new ImagePreviewPanel();
		fileChooser.setAccessory(preview);
		fileChooser.addPropertyChangeListener(preview);
		int result = fileChooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
		    // user selects a file
			printToconsole("Load Video: "+fileChooser.getSelectedFile().getName());
			if (fileChooser.getSelectedFile().toURI().toString().indexOf("%20") < 0) {
				UtilOpencv.openVideoFile(fileChooser.getSelectedFile().toURI().toString());
				//printToconsole("info : "+UtilOpencv.getFps()+" : "+UtilOpencv.getWidth()+" : "+UtilOpencv.getHeight());
				//paintBufferedImage(UtilOpencv.matToBufferedImage(UtilOpencv.grabFrameVideoFile()), null, true);
				paintSolidColor();
				frameCountVideo = 2;
				preInfoFrame(UtilOpencv.getFrameCountVideoFile(), UtilOpencv.getWidthVideoFile(), UtilOpencv.getHeightVideoFile(), UtilOpencv.getFpsVideoFile());
				debugFrame.setVisible(true);
			}
			else {
				printToconsole("ERROR: choose image of relative path without space in name.");
			}
		}
	}
	
	void preInfoFrame(int maxFrame, int width, int height, int fps){
		stepCountForward = 0;
		stepCountBackward = 0;
		//* JFrame for debug
		debugFrame = new JFrame("Info Video");
		debugFrame.setSize(340, 150);
		debugFrame.setBackground(Color.DARK_GRAY);
		debugFrame.setResizable(false);
		debugFrame.setLocation(100, 200);
		
		debugPanel = new JPanel();
		debugPanel.setLayout(new BoxLayout(debugPanel, BoxLayout.Y_AXIS));
		debugPanel.setBorder(BorderFactory.createTitledBorder("Progress"));
		pbar = new JProgressBar();
		pbar.setMinimum(MY_MINIMUM);
		pbar.setMaximum(maxFrame);
		pbar.setStringPainted(true);
		pbar.setAlignmentX(Component.CENTER_ALIGNMENT);
		// add to JPanel
		debugPanel.add(pbar);
		
		//TODO
		UtilOpencv.VideoInit("Output Video", fps);
		printToconsole(UtilOpencv.GetVideoSavedPath());
		
		
		String textInfo = new String("Info : "+fps+" fps >> "+width+" x "+height+" px");
		jlabelInfoVideo = new JLabel(textInfo);
		jlabelInfoVideo.setAlignmentX(Component.CENTER_ALIGNMENT);
		debugPanel.add(jlabelInfoVideo);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.setBorder(BorderFactory.createTitledBorder("ToolBar"));
		
		ImageIcon playButtonIcon = new ImageIcon("icons/play.png");
		playButton = new JButton(playButtonIcon);
		playButton.setToolTipText("Play Video File");
		playButton.setEnabled(false);
		playButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				if(!isPlay){
					isPlay = true;
					isStop = false;
					isPause = false;
					isForward = false;
					isBackward = false;
					printToconsole("Video file -> play");
					playButton.setEnabled(false);
					stopButton.setEnabled(true);
					pauseButton.setEnabled(true);
					forwardButton.setEnabled(true);
					backwardButton.setEnabled(true);
					stepCountBackward = 0;
					stepCountForward = 0;
					backwardButton.setText("");
					forwardButton.setText("");
				}
			}
		});
		buttonPanel.add(playButton);
		
		ImageIcon stopButtonIcon = new ImageIcon("icons/stop.png");
		stopButton = new JButton(stopButtonIcon);
		stopButton.setToolTipText("Stop Video File");
		stopButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				if(!isStop){
					isPlay = false;
					isStop = true;
					isPause = false;
					isForward = false;
					isBackward = false;
					printToconsole("Video file -> stop");
					playButton.setEnabled(true);
					stopButton.setEnabled(false);
					pauseButton.setEnabled(true);
					forwardButton.setEnabled(true);
					backwardButton.setEnabled(true);
					
					isFotoFilter = false;
					if (UtilOpencv.isVideoFileOpen()) {
						debugFrame.setVisible(false);
						UtilOpencv.closeVideoFile();
					}
										
					if(!UtilOpencv.IsVideoWriterOpen())
						UtilOpencv.CloseVideoWrite();
				}
			}
		});
		buttonPanel.add(stopButton);
		
		ImageIcon pauseButtonIcon = new ImageIcon("icons/pause.png");
		pauseButton = new JButton(pauseButtonIcon);
		pauseButton.setToolTipText("Pause Video File");
		pauseButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				if(!isPause){
					isPlay = false;
					isStop = false;
					isPause = true;
					isForward = false;
					isBackward = false;
					printToconsole("Video file -> pause");
					playButton.setEnabled(true);
					stopButton.setEnabled(true);
					pauseButton.setEnabled(false);
					forwardButton.setEnabled(true);
					backwardButton.setEnabled(true);
					stepCountBackward = 0;
					stepCountForward = 0;
					backwardButton.setText("");
					forwardButton.setText("");
				}
			}
		});
		buttonPanel.add(pauseButton);
		
		ImageIcon forwardButtonIcon = new ImageIcon("icons/forward.png");
		forwardButton = new JButton("");
		forwardButton.setIcon(forwardButtonIcon);
		forwardButton.setToolTipText("Forward Video File");
		forwardButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				if(!isForward){
					stepCountBackward = 0;
					stepCountForward = 0;
					backwardButton.setText("");
					
					forwardButton.setText(""+stepVideo[stepCountForward]);
					isPlay = false;
					isStop = false;
					isPause = false;
					isForward = true;
					isBackward = false;
					printToconsole("Video file -> forward");
					playButton.setEnabled(true);
					stopButton.setEnabled(true);
					pauseButton.setEnabled(true);
					forwardButton.setEnabled(true);
					backwardButton.setEnabled(true);
				}
				else
				{
					if (stepCountForward < 4){
						stepCountForward++;
						forwardButton.setText(""+stepVideo[stepCountForward]);
						printToconsole("Video file -> forward (x"+stepVideo[stepCountForward]+")");
					}
				}
			}
		});
		buttonPanel.add(forwardButton);
		
		ImageIcon backwardButtonIcon = new ImageIcon("icons/backward.png");
		backwardButton = new JButton("");
		backwardButton.setIcon(backwardButtonIcon);
		backwardButton.setToolTipText("Backward Video File");
		backwardButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				if(!isBackward){
					stepCountForward = 0;
					stepCountBackward = 0;
					forwardButton.setText("");
					
					backwardButton.setText(""+stepVideo[stepCountBackward]);
					isPlay = false;
					isStop = false;
					isPause = false;
					isForward = false;
					isBackward = true;
					printToconsole("Video file -> backward");
					playButton.setEnabled(true);
					stopButton.setEnabled(true);
					pauseButton.setEnabled(true);
					forwardButton.setEnabled(true);
					backwardButton.setEnabled(true);
				}
				else{
					if (stepCountBackward < 4){
						stepCountBackward++;
						backwardButton.setText(""+stepVideo[stepCountBackward]);
						printToconsole("Video file -> backward (x"+stepVideo[stepCountBackward]+")");
					}
				}
			}
		});
		buttonPanel.add(backwardButton);
		
		debugPanel.add(buttonPanel);
		
		debugFrame.add(debugPanel);
		
		isPlay = true;
		isStop = false;
		isPause = false;
		isForward = false;
		isBackward = false;
	}
	
	//FILTERS
	
	private BufferedImage FilterImageFusion(String pathImage, int w, int h) {
		//System.out.println(pathImage);
		Mat image = UtilOpencv.loadImageMat(pathImage);
        //new ImShow("original").showImage(image);
        fusion = FusionEnhance.enhance(image, level);
        fusion.convertTo(fusion, CvType.CV_8UC1);
        //ImShow show = new ImShow("fusion");
        //show.showImage(fusion);
        Imgproc.resize(fusion, fusion, new Size(w, h));
		bufImage = null;
		try {
			bufImage = showFusion.toBufferedImage(fusion);
		} catch (Exception e) {
			e.printStackTrace();
		}
        
		if(bufImage != null) {
			return bufImage;
		}
		else {
			System.out.println("imagem null 2");
			return null;
		}
	}
	
	private BufferedImage FilterImageFusion(Mat img, int w, int h) {
		fusion = FusionEnhance.enhance(img, level);
        fusion.convertTo(fusion, CvType.CV_8UC1);
        originalMat = img;
        originalMat.convertTo(originalMat, CvType.CV_8UC1);
        filterMat = fusion;
        filter = showFusion.toBufferedImage(fusion);
        Imgproc.resize(fusion, fusion, new Size(w, h));
                
        //show.showImage(fusion);
		bufImage = null;
		try {
			bufImage = showFusion.toBufferedImage(fusion);
		} catch (Exception e) {
			e.printStackTrace();
		}
        
		if(bufImage != null) {
			return bufImage;
		}
		else {
			System.out.println("imagem null 2");
			return null;
		}
	}
	
	private BufferedImage FilterImageFusion(BufferedImage image, int w, int h) {
		return FilterImageFusion(UtilOpencv.bufferedImageToMat(image), w, h);
	}

}