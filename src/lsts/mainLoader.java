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
import java.io.Serial;
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

public class mainLoader extends JFrame implements ActionListener {
  static {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
  }

  @Serial
  private static final long serialVersionUID = 1L;
  static JFrame consoleOutput;
  static JScrollPane consoleScroll;
  static JTextArea printTextOut = null;
  int mainFrameWidth = 480 * 2;
  int mainFrameHeight = 480;
  int widthFrame = mainFrameWidth;
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
  JMenuItem menuItemSaveImage;
  JFileChooser fileChooser;
  ImagePreviewPanel preview;
  Thread updaterThreadImage = null;
  boolean isPhotoFilter;
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
  int[] stepVideo = {2, 4, 6, 8, 10};
  int stepCountForward;
  int stepCountBackward;
  int frameCountVideo;
  JPanel debugPanel;
  JProgressBar pBar;
  static final int MY_MINIMUM = 0;
  JLabel jLabelInfoVideo;
  ImShow showFusion = new ImShow("fusion");
  ImShow showOriginal = new ImShow("original");
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

  public static void main(String[] args) {
    initConsoleFrame();
    printToConsole("Welcome");
    printToConsole("Operating System: " + UtilOpencv.getOS() + " - x" + UtilOpencv.getArch());
    printToConsole("Version of OpenCV (jar) : " + Core.VERSION);
    new mainLoader();
  }

  static void initConsoleFrame() {
    // * JFrame for debug
    consoleOutput = new JFrame("Console Output");
    consoleOutput.setSize(450, 254);
    consoleOutput.setBackground(Color.DARK_GRAY);
    consoleOutput.setResizable(false);
    consoleOutput.setLocation(100, 400);

    JPanel consolePanel = new JPanel();
    // consolePanel.setLayout(new BoxLayout(debugPanel, BoxLayout.Y_AXIS));
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

    consoleScroll = new JScrollPane(printTextOut, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    consolePanel.add(consoleScroll);
    consolePanel.add(clearText);

    consoleOutput.add(consolePanel);
    consoleOutput.setVisible(true);
  }

  static void printToConsole(String text) {
    //check if consoleOutput is displayed, if not display it
    if (!consoleOutput.isVisible()) {
      consoleOutput.setVisible(true);
    }
    Calendar nowHour = Calendar.getInstance();
    DecimalFormat mFormat = new DecimalFormat("00");
    printTextOut
        .append(mFormat.format(nowHour.get(Calendar.HOUR_OF_DAY)) + ":" + mFormat.format(nowHour.get(Calendar.MINUTE))
            + ":" + mFormat.format(nowHour.get(Calendar.SECOND)) + " >" + text + "\n");
    printTextOut.setCaretPosition(printTextOut.getDocument().getLength());
  }

  public mainLoader() {
    createAndShowMainGUI();
  }

  private void createAndShowMainGUI() {
    setTitle("Main Frame");
    setLocation(150, 20);
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    setResizable(true);
    setLocation(600, 200);
    setLayout(new MigLayout());

    showOriginal.setVisibleWindow(false);
    showFusion.setVisibleWindow(false);
    showRemoveBackScatter.setVisibleWindow(false);

    isPhotoFilter = false;
    setFrame();
    setMenu();
    paintSolidColor();
    //addZoomBox();

    updaterThreadImage = updaterThread();
    updaterThreadImage.start();

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        int i = JOptionPane.showConfirmDialog(null, "Are you sure you want to quit?", "", JOptionPane.YES_NO_OPTION);
        if (i == JOptionPane.YES_OPTION) {
          System.exit(0);
        }
      }
    });

    addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent evt) {
        Component c = evt.getComponent();
        widthFrame = c.getSize().width - 8;
        heightFrame = c.getSize().height - 60;
        // if (isFotoFilter)
        paintBufferedImage(null, null, UtilOpencv.isVideoFileOpen());
      }
    });
  }

  private void paintSolidColor() {
    BufferedImage solidColor = new BufferedImage(350, 350, BufferedImage.TYPE_3BYTE_BGR);
    Graphics2D graphics = solidColor.createGraphics();
    graphics.setColor(Color.BLACK);
    graphics.fillRect(0, 0, solidColor.getWidth(), solidColor.getHeight());
    graphics.dispose();
    paintBufferedImage(solidColor, null, false);
  }

  private void setFrame() {
    fullScreenPanel1 = false;
    fullScreenPanel2 = false;
    fullScreenPanel = new JFrame();
    fullScreenPanel.setExtendedState(JFrame.MAXIMIZED_BOTH);
    fullScreenPanel.setUndecorated(true);
    fullScreenPanel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && !UtilOpencv.isVideoFileOpen()) {
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
    panel1.setPreferredSize(new Dimension(mainFrameWidth / 2, mainFrameHeight));
    panel1.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && !UtilOpencv.isVideoFileOpen()) {
          fullScreenPanel1 = true;
          fullScreenPanel2 = false;
          fullScreenPanel.setVisible(true);
        }
      }
    });
    mainPanel.add(panel1);

    panel2 = new JPanel();
    panel2.setBorder(BorderFactory.createTitledBorder("Filtered"));
    panel2.setPreferredSize(new Dimension(mainFrameWidth / 2, mainFrameHeight));
    panel2.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && !UtilOpencv.isVideoFileOpen()) {
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

  private void paintBufferedImage(BufferedImage image, String path, boolean videoFile) {
    if (image != null) {
      imageBack = image;
    }
    if (!fullScreenPanel1 && !fullScreenPanel2) {
      if (isPhotoFilter) {
        if (path != null) {
          imageBackFilter = FilterImageFusion(path, (widthFrame / 2) - 24, heightFrame - 32);
          mainImage = imageBackFilter;
        } else {
          if (imageBackFilter != null) {
            mainImage = UtilOpencv.resize(imageBackFilter, (widthFrame / 2) - 24, heightFrame - 32);
          }
        }
      } else if (videoFile) {
        mainImage = FilterImageFusion(imageBack, (widthFrame / 2) - 24, heightFrame - 32);
      } else {
        mainImage = UtilOpencv.resize(imageBack, (widthFrame / 2) - 24, heightFrame - 32);
      }
      if (mainImage != null) {
        imageTemp2.setIcon(new ImageIcon(mainImage));
      }
      panel2.add(imageTemp2);
      mainPanel.add(panel2);

      mainImage = UtilOpencv.resize(imageBack, (widthFrame / 2) - 24, heightFrame - 32);
      imageTemp1.setIcon(new ImageIcon(mainImage));
      panel1.add(imageTemp1);
      mainPanel.add(panel1);

      mainPanel.repaint();
      revalidate();
      repaint();

      if (isPlay) {
        // TODO
        if (!UtilOpencv.saveSnapshot(UtilOpencv.joinBufferedImage(original, filter), "Output Images"))
          printToConsole("ERROR: save snapshot image failed");
        /*
         * System.out.println("type:"+originalMat.type()+" | "+filterMat.type());
         * System.out.println("rows:"+originalMat.rows()+" | "+filterMat.rows());
         * System.out.println("cols:"+originalMat.cols()+" | "+filterMat.cols());
         * System.out.println("dims:"+originalMat.dims()+" | "+filterMat.dims());
         */

        filterMat = UtilOpencv.resizeMat(filterMat, originalMat.rows(), originalMat.cols());

        /*
         * System.out.println("type:"+originalMat.type()+" | "+filterMat.type());
         * System.out.println("rows:"+originalMat.rows()+" | "+filterMat.rows());
         * System.out.println("cols:"+originalMat.cols()+" | "+filterMat.cols());
         * System.out.println("dims:"+originalMat.dims()+" | "+filterMat.dims()+"\n\n");
         */
        UtilOpencv.AddVideoFrame(UtilOpencv.joinMatImage(originalMat, filterMat), filterMat);
      }
    } else {
      if (fullScreenPanel2) {
        if (isPhotoFilter) {
          if (path != null) {
            imageBackFilter = FilterImageFusion(path, fullScreenPanel.getWidth(), fullScreenPanel.getHeight());
            mainImage = imageBackFilter;
          } else {
            if (imageBackFilter != null)
              mainImage = UtilOpencv.resize(imageBackFilter, fullScreenPanel.getWidth(), fullScreenPanel.getHeight());
          }
        } else if (videoFile)
          mainImage = FilterImageFusion(imageBack, fullScreenPanel.getWidth(), fullScreenPanel.getHeight());
        else
          mainImage = UtilOpencv.resize(imageBack, fullScreenPanel.getWidth(), fullScreenPanel.getHeight());

        if (mainImage != null) {
          imageTemp2.setIcon(new ImageIcon(mainImage));
        }
        fullScreenPanel.add(imageTemp2);
      } else {
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
    menuItemSaveImage = new JMenuItem("Save Image");
    menuItemSaveImage.addActionListener(this);
    menuOptions.add(menuItemSaveImage);
    menuItemVideo = new JMenuItem("Load a Video");
    menuItemVideo.addActionListener(this);
    menuOptions.add(menuItemVideo);
    menuBar.add(menuOptions);
    setJMenuBar(menuBar);
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    if (arg0.getActionCommand().equals("Load a Image")) {
      filterImage();
    } else if (arg0.getActionCommand().equals("Load a Video")) {
      filterVideoFile();
    } else if (arg0.getActionCommand().equals("Save Image")) {
      saveImage();
    }
  }

  private void filterImage() {
    try {
      debugFrame.setVisible(false);
    } catch (Exception e) {
      // TODO: handle exception
    }
    // System.out.println("load image");
    FileNameExtensionFilter filter = new FileNameExtensionFilter("Image file", "jpeg", "png", "jpg");
    fileChooser = new JFileChooser();
    fileChooser.setFileFilter(filter);
    fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir") + "/media"));
    fileChooser.setDialogTitle("Image");
    preview = new ImagePreviewPanel();
    fileChooser.setAccessory(preview);
    fileChooser.addPropertyChangeListener(preview);

    int result = fileChooser.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      // user selects a file
      printToConsole("Load Image: " + fileChooser.getSelectedFile().getName());

      if (!fileChooser.getSelectedFile().toURI().toString().contains("%20")) {
        isPhotoFilter = true;
        paintBufferedImage(UtilOpencv.loadImage(fileChooser.getSelectedFile().toURI().toString()),
            fileChooser.getSelectedFile().toURI().toString(), false);
      } else {
        printToConsole("ERROR: choose image of relative path without space in name.");
      }
    }
  }

  @SuppressWarnings("unused")
  private void processAllFilter() {
    imageFrameVideo = UtilOpencv.grabFrameVideoFile();
    // FUSION

    fusion = FusionEnhance.enhance(imageFrameVideo, level);
    fusion.convertTo(fusion, CvType.CV_8UC1);
    showFusion.showImage(fusion);

    // Remove scatter
    /*
     * fusion = RemoveBackScatter.enhance(imageFrameVideo, blkSize, patchSize,
     * lambda, gamma, r, eps, level);
     * fusion.convertTo(fusion, CvType.CV_8UC1);
     * showRemoveBackScatter.showImage(fusion);
     */

    showOriginal.showImage(imageFrameVideo);
  }

  private void getVideoFileFrame() {
    Mat frameOriginal = UtilOpencv.grabFrameVideoFile();
    Mat frame = null;
    if (frameOriginal != null) {
      frame = UtilOpencv.resizeMat(frameOriginal, 375, 500);
    }
    if (frame != null) {
      original = showFusion.toBufferedImage(frame);
    }
    paintBufferedImage(UtilOpencv.matToBufferedImage(frame), null, true);
    // processAllFilter();

    if (isForward) {
      frameCountVideo = frameCountVideo + stepVideo[stepCountForward];
      pBar.setValue(frameCountVideo);
    } else if (isBackward) {
      frameCountVideo = (int) UtilOpencv.getFramePosition();
      pBar.setValue(frameCountVideo);
    } else {
      frameCountVideo++;
      pBar.setValue(frameCountVideo);
    }

    if (isBackward && (int) UtilOpencv.getFramePosition() < 10) {
      isPlay = false;
      isStop = false;
      isPause = true;
      isForward = false;
      isBackward = false;
      printToConsole("Video file -> pause");
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
    return new Thread("Update Image Thread") {
      @Override
      public void run() {
        while (true) {
          if (isPhotoFilter) {
            try {
              Thread.sleep(1000);
            } catch (InterruptedException e) {
              printToConsole("ERROR 1: " + e.getMessage());
            }
            paintBufferedImage(null, null, false);
          } else {
            if (UtilOpencv.isVideoFileOpen()) {
              if (isPlay) {
                try {
                  long startTime = System.currentTimeMillis();
                  getVideoFileFrame();
                  long stopTime = System.currentTimeMillis();
                  long fpsTime = stopTime - startTime;
                  printToConsole("time used:" + fpsTime + "ms");
                  long fpsFinal = 1000 / UtilOpencv.getFpsVideoFile();
                  while (fpsTime < fpsFinal) {
                    stopTime = System.currentTimeMillis();
                    fpsTime = stopTime - startTime;
                  }
                } catch (Exception e) {
                  try {
                    debugFrame.setVisible(false);
                    System.out.println("A:" + e.getMessage());
                  } catch (Exception e1) {
                    printToConsole("ERROR 2: " + e1.getMessage());
                  }

                  UtilOpencv.closeVideoFile();
                }
              } else if (isForward) {
                try {
                  long startTime = System.currentTimeMillis();
                  if (!UtilOpencv.jumpFramesForward(stepVideo[stepCountForward]))
                    printToConsole("ERROR: jump frames forward failed");
                  getVideoFileFrame();
                  long stopTime = System.currentTimeMillis();
                  long fpsTime = stopTime - startTime;
                  long fpsFinal = 1000 / UtilOpencv.getFpsVideoFile();
                  while (fpsTime < fpsFinal) {
                    stopTime = System.currentTimeMillis();
                    fpsTime = stopTime - startTime;
                  }
                } catch (Exception e) {
                  try {
                    debugFrame.setVisible(false);
                    System.out.println("B:" + e.getMessage());
                  } catch (Exception e1) {
                    printToConsole("ERROR 3: " + e1.getMessage());
                  }

                  UtilOpencv.closeVideoFile();
                }
              } else if (isBackward) {
                try {
                  long startTime = System.currentTimeMillis();
                  if (!UtilOpencv.jumpFramesBackward(stepVideo[stepCountBackward]))
                    printToConsole("ERROR: jumpFramesBackward failed");
                  getVideoFileFrame();
                  long stopTime = System.currentTimeMillis();
                  long fpsTime = stopTime - startTime;
                  long fpsFinal = 1000 / UtilOpencv.getFpsVideoFile();
                  while (fpsTime < fpsFinal) {
                    stopTime = System.currentTimeMillis();
                    fpsTime = stopTime - startTime;
                  }
                } catch (Exception e) {
                  try {
                    debugFrame.setVisible(false);
                    System.out.println("C:" + e.getMessage());
                  } catch (Exception ignored) {
                    printToConsole("ERROR 4: " + e.getMessage());
                  }

                  UtilOpencv.closeVideoFile();
                }
              } else {
                try {
                  Thread.sleep(1000);
                  paintBufferedImage(null, null, true);
                } catch (InterruptedException err) {
                  printToConsole("ERROR 5: " + err.getMessage());
                }
              }
            } else {
              try {
                Thread.sleep(1000);
              } catch (InterruptedException e) {
                printToConsole("ERROR 6: " + e.getMessage());
              }
            }
          }
        }
      }
    };
  }

  public void filterVideoFile() {
    isPhotoFilter = false;

    FileNameExtensionFilter filter = new FileNameExtensionFilter("Video file", "avi", "mp4", "mjpeg", "3gp", "mov",
        "MOV", "wmv", "mkv");
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
      printToConsole("Load Video: " + fileChooser.getSelectedFile().getName());
      if (!fileChooser.getSelectedFile().toURI().toString().contains("%20")) {
        UtilOpencv.openVideoFile(fileChooser.getSelectedFile().toURI().toString());
        // printToConsole("info : "+UtilOpencv.getFps()+" : "+UtilOpencv.getWidth()+" :
        // "+UtilOpencv.getHeight());
        // paintBufferedImage(UtilOpencv.matToBufferedImage(UtilOpencv.grabFrameVideoFile()),
        // null, true);
        paintSolidColor();
        frameCountVideo = 2;
        preInfoFrame(UtilOpencv.getFrameCountVideoFile(), UtilOpencv.getWidthVideoFile(),
            UtilOpencv.getHeightVideoFile(), UtilOpencv.getFpsVideoFile());
        debugFrame.setVisible(true);
      } else {
        printToConsole("ERROR: choose image of relative path without space in name.");
      }
    }
  }

  void preInfoFrame(int maxFrame, int width, int height, int fps) {
    stepCountForward = 0;
    stepCountBackward = 0;
    // * JFrame for debug
    debugFrame = new JFrame("Info Video");
    debugFrame.setSize(340, 150);
    debugFrame.setBackground(Color.DARK_GRAY);
    debugFrame.setResizable(false);
    debugFrame.setLocation(100, 200);

    debugPanel = new JPanel();
    debugPanel.setLayout(new BoxLayout(debugPanel, BoxLayout.Y_AXIS));
    debugPanel.setBorder(BorderFactory.createTitledBorder("Progress"));
    pBar = new JProgressBar();
    pBar.setMinimum(MY_MINIMUM);
    pBar.setMaximum(maxFrame);
    pBar.setStringPainted(true);
    pBar.setAlignmentX(Component.CENTER_ALIGNMENT);
    // add to JPanel
    debugPanel.add(pBar);

    // TODO
    UtilOpencv.VideoInit("Output Video", fps);
    printToConsole(UtilOpencv.GetVideoSavedPath());

    String textInfo = "Info : " + fps + " fps >> " + width + " x " + height + " px";
    jLabelInfoVideo = new JLabel(textInfo);
    jLabelInfoVideo.setAlignmentX(Component.CENTER_ALIGNMENT);
    debugPanel.add(jLabelInfoVideo);

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
    buttonPanel.setBorder(BorderFactory.createTitledBorder("ToolBar"));

    ImageIcon playButtonIcon = new ImageIcon("icons/play.png");
    playButton = new JButton(playButtonIcon);
    playButton.setToolTipText("Play Video File");
    playButton.setEnabled(false);
    playButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (!isPlay) {
          isPlay = true;
          isStop = false;
          isPause = false;
          isForward = false;
          isBackward = false;
          printToConsole("Video file -> play");
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
        if (!isStop) {
          isPlay = false;
          isStop = true;
          isPause = false;
          isForward = false;
          isBackward = false;
          printToConsole("Video file -> stop");
          playButton.setEnabled(true);
          stopButton.setEnabled(false);
          pauseButton.setEnabled(true);
          forwardButton.setEnabled(true);
          backwardButton.setEnabled(true);

          isPhotoFilter = false;
          if (UtilOpencv.isVideoFileOpen()) {
            debugFrame.setVisible(false);
            UtilOpencv.closeVideoFile();
          }

          if (!UtilOpencv.IsVideoWriterOpen())
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
        if (!isPause) {
          isPlay = false;
          isStop = false;
          isPause = true;
          isForward = false;
          isBackward = false;
          printToConsole("Video file -> pause");
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
        if (!isForward) {
          stepCountBackward = 0;
          stepCountForward = 0;
          backwardButton.setText("");

          forwardButton.setText("" + stepVideo[stepCountForward]);
          isPlay = false;
          isStop = false;
          isPause = false;
          isForward = true;
          isBackward = false;
          printToConsole("Video file -> forward");
          playButton.setEnabled(true);
          stopButton.setEnabled(true);
          pauseButton.setEnabled(true);
          forwardButton.setEnabled(true);
          backwardButton.setEnabled(true);
        } else {
          if (stepCountForward < 4) {
            stepCountForward++;
            forwardButton.setText("" + stepVideo[stepCountForward]);
            printToConsole("Video file -> forward (x" + stepVideo[stepCountForward] + ")");
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
        if (!isBackward) {
          stepCountForward = 0;
          stepCountBackward = 0;
          forwardButton.setText("");

          backwardButton.setText("" + stepVideo[stepCountBackward]);
          isPlay = false;
          isStop = false;
          isPause = false;
          isForward = false;
          isBackward = true;
          printToConsole("Video file -> backward");
          playButton.setEnabled(true);
          stopButton.setEnabled(true);
          pauseButton.setEnabled(true);
          forwardButton.setEnabled(true);
          backwardButton.setEnabled(true);
        } else {
          if (stepCountBackward < 4) {
            stepCountBackward++;
            backwardButton.setText("" + stepVideo[stepCountBackward]);
            printToConsole("Video file -> backward (x" + stepVideo[stepCountBackward] + ")");
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

  private void saveImage() {
    if(UtilOpencv.saveSnapshot(UtilOpencv.joinBufferedImage(mainImage, imageBackFilter), "Output Images"))
      printToConsole("Image saved");
    else
      printToConsole("ERROR: save image failed");
  }

  // TODO
  // Need to add a square box showing zoom where the mouse is hover filtered image
  private void addZoomBox() {
    panel2.addMouseMotionListener(new MouseAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        int zoomSize = 100;
        int x = e.getX();
        int y = e.getY();
        int startX = Math.max(0, x - zoomSize / 2);
        int startY = Math.max(0, y - zoomSize / 2);
        int endX = Math.min(mainImage.getWidth(), x + zoomSize / 2);
        int endY = Math.min(mainImage.getHeight(), y + zoomSize / 2);

        BufferedImage zoomedImage = mainImage.getSubimage(startX, startY, endX - startX, endY - startY);
        BufferedImage scaledZoomedImage = new BufferedImage(zoomSize * 2, zoomSize * 2, zoomedImage.getType());
        Graphics2D g = scaledZoomedImage.createGraphics();
        g.drawImage(zoomedImage, 0, 0, zoomSize * 2, zoomSize * 2, null);
        g.dispose();

        JLabel zoomLabel = new JLabel(new ImageIcon(scaledZoomedImage));
        zoomLabel.setBorder(BorderFactory.createLineBorder(Color.RED));
        zoomLabel.setBounds(x + 10, y + 10, zoomSize * 2, zoomSize * 2);
        panel2.add(zoomLabel);
        panel2.repaint();
      }
    });
  }

  // FILTERS

  private BufferedImage FilterImageFusion(String pathImage, int w, int h) {
    // System.out.println(pathImage);
    Mat image = UtilOpencv.loadImageMat(pathImage);
    // new ImShow("original").showImage(image);
    fusion = FusionEnhance.enhance(image, level);
    fusion.convertTo(fusion, CvType.CV_8UC1);
    // ImShow show = new ImShow("fusion");
    // show.showImage(fusion);
    Imgproc.resize(fusion, fusion, new Size(w, h));
    bufImage = null;
    try {
      bufImage = showFusion.toBufferedImage(fusion);
    } catch (Exception e) {
      printToConsole("ERROR: " + e.getMessage());
    }

    if (bufImage != null) {
      return bufImage;
    } else {
      System.out.println("image null 2");
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

    // show.showImage(fusion);
    bufImage = null;
    try {
      bufImage = showFusion.toBufferedImage(fusion);
    } catch (Exception e) {
      printToConsole("ERROR: " + e.getMessage());
    }

    if (bufImage != null) {
      return bufImage;
    } else {
      System.out.println("image null 2");
      return null;
    }
  }

  private BufferedImage FilterImageFusion(BufferedImage image, int w, int h) {
    return FilterImageFusion(UtilOpencv.bufferedImageToMat(image), w, h);
  }

}