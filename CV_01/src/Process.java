// Copyright (C) 2009 by Klaus Jung
// angepasst von Kai Barthel

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.*;
import java.awt.*;
import java.io.File;

public class Process extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private static final int border = 10;
	private static final int maxWidth = 600;
	private static final int maxHeight = 600;

	
	private static JFrame frame;
	
	private ImageView srcView;			// source image view
	private ImageView accView;
	private ImageView dstView;			// scaled image view
	
	private JComboBox methodList;		// the selected scaling method
	private JLabel statusLine;			// to print some status text
	private JTextField parameterInput1;		// to input a scaling factor
	private double parameter1 = 1;		// initial scaling factor

	public Process() {
        super(new BorderLayout(border, border));
        
        // load the default image
        File input = new File("linien.png");
        
        if(!input.canRead()) input = openFile(); // file not found, choose another image
        
        srcView = new ImageView(input);
        srcView.setMaxSize(new Dimension(maxWidth, maxHeight));
       
        accView = new ImageView(maxWidth, maxHeight);
		// create an empty destination image
		dstView = new ImageView(maxWidth, maxHeight);
		
		// load image button
        JButton load = new JButton("Bild Oeffnen");
        load.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		File input = openFile();
        		if(input != null) {
	        		srcView.loadImage(input);
	        		srcView.setMaxSize(new Dimension(maxWidth, maxHeight));
	                processImage(true);
        		}
        	}        	
        });
         
        // selector for the method
        JLabel methodText = new JLabel("Methode:");
        String[] methodNames = {"Kopie", "Graustufen", "X-Gradient", 
        		"Y-Gradient", "X-Gradient Sobel (sep)", "Y-Gradient Sobel (sep)", "X-Gradient Sobel", "Y-Gradient Sobel", 
        		"Gradientenbetrag", "Gradientenwinkel", "Gradientenwinkel Farbe", "Kombination", "Finde Linien"};
        
        methodList = new JComboBox(methodNames);
        methodList.setSelectedIndex(methodNames.length - 1);		// set initial method
        methodList.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
                processImage(false);
        	}
        });
        
        // input for scaling factor
        JLabel scaleText = new JLabel("Parameter:");
         
        parameterInput1 = new JTextField(8);
        parameterInput1.setText(String.valueOf(parameter1));
        parameterInput1.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
                processImage(false);
        	}        	
        });
        
        // apply button
        JButton apply = new JButton("Ausfuehren");
        apply.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
                processImage(false);
        	}        	
        });
        
        // some status text
        statusLine = new JLabel(" ");
        
        // arrange all controls
        JPanel controls = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0,border,0,0);
        controls.add(load, c);
        controls.add(methodText, c);
        controls.add(methodList, c);
        controls.add(scaleText, c);
        controls.add(parameterInput1, c);
        controls.add(apply, c);
        
        JPanel images = new JPanel(new FlowLayout());
        images.add(srcView);
        images.add(accView);
        images.add(dstView);
        
        add(controls, BorderLayout.NORTH);
        add(images, BorderLayout.CENTER);
        add(statusLine, BorderLayout.SOUTH);
               
        setBorder(BorderFactory.createEmptyBorder(border,border,border,border));
        
        // perform the initial scaling
        processImage(true);
	}
	
	private File openFile() {
        JFileChooser chooser = new JFileChooser("/Users/simon/Documents/HTW/5. Semester/CV/workspace/CV/CV_01");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Images (*.jpg, *.png, *.gif)", "jpg", "png", "gif");
        chooser.setFileFilter(filter);
        int ret = chooser.showOpenDialog(this);
        if(ret == JFileChooser.APPROVE_OPTION) return chooser.getSelectedFile();
        return null;		
	}
    
	private static void createAndShowGUI() {
		// create and setup the window
		frame = new JFrame("Computer Vision 1");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JComponent newContentPane = new Process();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        // display the window.
        frame.pack();
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
        frame.setVisible(true);
	}

	public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
	}
	
	
    protected void processImage(boolean silent) {
  
        String methodName = (String)methodList.getSelectedItem();
    	
    	try  {
    		parameter1 = Double.parseDouble(parameterInput1.getText());
    	} catch(Exception e) {
    		 JOptionPane.showMessageDialog(this, "Bitte geben Sie eine Zahl ein.", "Eingabefehler", JOptionPane.ERROR_MESSAGE);
    		 return;
    	}
    	
        
    	// get image dimensions
    	int width = srcView.getImgWidth();
    	int height = srcView.getImgHeight();


    	// get pixels arrays
    	int srcPixels[] = srcView.getPixels();
    	int dstPixels[] = new int[width * height];
    	int accPixels[] = new int[width * height];
    	
    	for(int q = 0; q < accPixels.length; q++) {
    		accPixels[q] = -16777216;
    	}
    	
    	String message = "\"" + methodName + "\"";

    	statusLine.setText(message);
    	int filterWidth;
    	int filterHeight;
		float[] sobelX = {-1, 0, 1,
				-2, 0, 2,
				-1, 0, 1};
		
		float[] sobelY = {-1, -2, -1,
				0, 0, 0,
				1, 2, 1};
		int[] tempSobelX = new int[width * height];
		int[] tempSobelY = new int[width * height];
		
		long startTime = System.currentTimeMillis();

    	switch(methodList.getSelectedIndex()) {
    	case 0:	// Kopie
    		doCopy(srcPixels, dstPixels, width, height);
    		break;
    	case 1:	// Graustufen
    		doGray(srcPixels, dstPixels, width, height);
    		break;
    	case 2:	// X-Gradient
    		float[] xGradientFilter = {-0.5f, 0, 0.5f};
    		doSimpleHorizontalConvolution(srcPixels, dstPixels, width, height, xGradientFilter);
    		break;
    	case 3:	// Y-Gradient
    		float[] yGradientFilter = {-0.5f, 0, 0.5f};
    		doSimpleVerticalConvolution(srcPixels, dstPixels, width, height, yGradientFilter);
    		break;
    	case 4:	// X-Gradient Sobel (sep)
    		float[] sobelFilterX1 = {1, 2, 1};
    		int[] tempX1 = new int[width * height];
    		doSimpleVerticalConvolution(srcPixels, tempX1, width, height, sobelFilterX1);
    		float[] sobelFilterX2 = {-1, 0, 1};
    		int[] tempX2 = new int[width * height];
    		doSimpleHorizontalConvolution(srcPixels, tempX2, width, height, sobelFilterX2);
    		showFilteredPixelsOfJoinedConvolutions(tempX1, tempX2, dstPixels);
    		break;
    	case 5:	// Y-Gradient Sobel (sep)
    		float[] sobelFilterY1 = {-1, 0, 1};
    		int[] tempY1 = new int[width * height];
    		doSimpleVerticalConvolution(srcPixels, tempY1, width, height, sobelFilterY1);
    		float[] sobelFilterY2 = {1, 2, 1};
    		int[] tempY2 = new int[width * height];
    		doSimpleHorizontalConvolution(srcPixels, tempY2, width, height, sobelFilterY2);
    		showFilteredPixelsOfJoinedConvolutions(tempY1, tempY2, dstPixels);
    		break;
    	case 6:	// "X-Gradient Sobel

    		filterWidth = 3;
    		filterHeight = 3;
    		int[] tempXGradSobel = new int[width * height];
    		doConvolution(srcPixels, tempXGradSobel, width, height, sobelX, filterWidth, filterHeight);
    		showDstPixels(tempXGradSobel, dstPixels);
    		
    		break;
    	case 7:	// Y-Gradient Sobel
    		filterWidth = 3;
    		filterHeight = 3;    
    		int[] tempYGradSobel = new int[width * height];
    		doConvolution(srcPixels, tempYGradSobel, width, height, sobelY, filterWidth, filterHeight);
    		showDstPixels(tempYGradSobel, dstPixels);
    		break;
    	case 8:	// Gradientenbetrag

    		filterWidth = 3;
    		filterHeight = 3;
    		doConvolution(srcPixels, tempSobelX, width, height, sobelX, filterWidth, filterHeight);
    		doConvolution(srcPixels, tempSobelY, width, height, sobelY, filterWidth, filterHeight);
    		showMagnitudeOfJoinedConvolutions(tempSobelX, tempSobelY, dstPixels);
    		break;
    	case 9:	// Gradientenwinkel
    		filterWidth = 3;
    		filterHeight = 3;
    		doConvolution(srcPixels, tempSobelX, width, height, sobelX, filterWidth, filterHeight);
    		doConvolution(srcPixels, tempSobelY, width, height, sobelY, filterWidth, filterHeight);
    		showAngleOfJoinedConvolutions(tempSobelX, tempSobelY, dstPixels);
    		break;
    	case 10:	// Gradientenwinkel Farbe
    		
    		break;
    	case 11:	// Kombination
    		filterWidth = 3;
    		filterHeight = 3;
    		doConvolution(srcPixels, tempSobelX, width, height, sobelX, filterWidth, filterHeight);
    		doConvolution(srcPixels, tempSobelY, width, height, sobelY, filterWidth, filterHeight);
    		showColoredPixelsOfJoinedConvolutions(tempSobelX, tempSobelY, dstPixels);
    		break;
    	case 12:	// Kombination
    		//findLines(srcPixels, accPixels, dstPixels, width, height);
    		linearHoughTransformation(srcPixels, accPixels, dstPixels, width, height);
    		break;
    	default:	
    		break;
    	}

		long time = System.currentTimeMillis() - startTime;
		   	
        dstView.setPixels(dstPixels, width, height);
        
        accView.setPixels(accPixels, width, height);
        
        frame.pack();
        
    	statusLine.setText(message + " in " + time + " ms");
    }
    private void findLines(int srcPixels[], int accPixels[], int dstPixels[], int width, int height) {
		int angleAmount = 18; // 0 - 170 degrees
		int angleStep = 10;
		int xCenter = width / 2;
		int yCenter = height / 2;

		double[] sinThetas = new double[angleAmount];
		double[] cosThetas = new double[angleAmount];
		for(int a = 0; a < angleAmount ; a++) {
			int angle = a*10;
			sinThetas[a] = Math.sin(Math.toRadians(angle));
			cosThetas[a] = Math.cos(Math.toRadians(angle));
		}
		//System.out.println("length: " + srcPixels.length);
		//System.out.println(width); // 400
		//System.out.println(height); // 256
		//accumulatorPic
    	for (int y = 0; y < height; y++) {
    		for (int x = 0; x < width; x++) {
    			int pos	= y * width + x;
    			int pixel = srcPixels[pos];
    			//found white pixel?
    			if(pixel == -1) {
    				int xp = x - xCenter;
    				int yp = y - yCenter;
    				for(int i=0; i<angleAmount; i++) {
    					int theta = (int) (Math.toRadians(i*angleStep) / Math.PI * width);
    					int r = (int) Math.round((xp * cosThetas[i] + yp * sinThetas[i]));
    					int location = r * width + theta;
    					//System.out.println("theta: " + theta + " r: " + r);
    					accPixels[location] += 10000;
    				}
    			}
    		}
    	}
	}
    
    private void linearHoughTransformation(int srcPixels[], int accPixels[], int dstPixels[], int width, int height) {
    	int xCtr = width / 2;
    	int yCtr = height / 2;
    	int nAng = 256;
    	double dAng = Math.PI / nAng;
    	int nRad = 256;
    	double rMax = Math.sqrt(xCtr*xCtr + yCtr*yCtr);
    	double dRad = (2*rMax)/nRad;
    	int[][] houghArray = new int[nAng][nRad];
    	for(int v=0; v<height; v++) {
    		for(int u=0; u<width; u++) {
    			int pos	= v * width + u;
    			if(srcPixels[pos] == -1) {
    				//doPixel
    				int x = u-xCtr;
    				int y = v-yCtr;
    				for(int a = 0; a < nAng; a++) {
    					double theta = dAng * a;
    					int r = (int) Math.round((x*Math.cos(theta) + y*Math.sin(theta)) / dRad) + nRad / 2;
    					if (r >= 0 && r < nRad) {
    						houghArray[a][r]++;
    						int lum = accPixels[r*width+a] + 1;
    						accPixels[r*width+a] = 0xFF000000 | (lum<<16) | (lum<<8) | lum;
    					}
    				}
    			}
    		}
    	}
    	
    }

	void doCopy(int srcPixels[], int dstPixels[], int width, int height) {
    	// loop over all pixels of the destination image

    	for (int y = 0; y < height; y++) {

    		for (int x = 0; x < width; x++) {

    			int pos	= y * width + x;

    			dstPixels[pos] = srcPixels[pos];

    		}
    	}
    }
    
    void doGray(int srcPixels[], int dstPixels[], int width, int height) {
		// loop over all pixels of the destination image
		
		for (int y = 0; y < height; y++) {
			
			for (int x = 0; x < width; x++) {
				
					int pos	= y * width + x;
				
					int c = srcPixels[pos];
					int r = (c>>16)&0xFF;
					int g = (c>> 8)&0xFF;
					int b = (c    )&0xFF;
					
					int lum = (int) (0.299*r + 0.587*g + 0.114*b + parameter1);
					lum = Math.min(lum,255);
					dstPixels[pos] = 0xFF000000 | (lum<<16) | (lum<<8) | lum;
				
			}
		}
    }
    
    void doSimpleHorizontalConvolution(int srcPixels[], int dstPixels[], int width, int height, float[] filter) {
    	float filterWeight = getFilterWeight(filter);
    	for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pos	= y * width + x;
				int left = (pos-1 < 0 || x % width == 0) ? (srcPixels[pos]>>8)&0xFF : (srcPixels[pos-1]>>8)&0xFF;
				int middle = srcPixels[pos];
				int right = (pos+1 >= srcPixels.length || x % (width - 1) == 0) ? (srcPixels[pos]>>8)&0xFF : (srcPixels[pos+1]>>8)&0xFF; 
				
				int result = (int) (left * filter[0] + middle * filter[1] + right * filter[2]);
				result /= filterWeight;
				
				int lum = (int) (0.299*result + 0.587*result + 0.114*result + parameter1);
				dstPixels[pos] = 0xFF000000 | (lum<<16) | (lum<<8) | lum;
				
			}
		}
    }
    
    void doSimpleVerticalConvolution(int srcPixels[], int dstPixels[], int width, int height, float[] filter) {
    	float filterWeight = getFilterWeight(filter);
    	for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pos	= y * width + x;
				int top = (pos-width < 0) ? (srcPixels[pos]>>8)&0xFF : (srcPixels[pos-width]>>8)&0xFF;
				int middle = srcPixels[pos];
				int bottom = (pos+width >= srcPixels.length) ? (srcPixels[pos]>>8)&0xFF : (srcPixels[pos+width]>>8)&0xFF; 
				
				int result = (int) (top * filter[0] + middle * filter[1] + bottom * filter[2]);
				result /= filterWeight;
				
				int lum = (int) (0.299*result + 0.587*result + 0.114*result + parameter1);
				dstPixels[pos] = 0xFF000000 | (lum<<16) | (lum<<8) | lum;
				
			}
		}
    }

    
    void doConvolution(int srcPixels[], int dstPixels[], int width, int height, float filter[], int filterWidth, int filterHeight) {
    	int[] temp = new int[width * height];
    	doGray(srcPixels, temp, width, height);
    	
    	float filterWeight = getFilterWeight(filter);
    	
    	for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pos = y * width + x;
				int counter = 0;
				int result = 0;
				int[] values = new int[filter.length];
				for(int j=pos-(filterHeight/2)*width; j<=pos+(filterHeight/2)*width; j = j+width) {
					for(int i=j-(filterWidth/2); i<=j+(filterWidth/2); i++)
					{
						if(i<0 || i>=temp.length || (i != 0 && pos % width == 0 && (i+1) % width == 0) || (pos != 0 && (pos+1) % width == 0 && i % width == 0)) 
							values[counter] = values[counter] = (temp[pos]>>8)&0xFF;//(getLumOfPixel(temp[pos]));//
						else 
							values[counter] = values[counter] = (temp[i]>>8)&0xFF;//(getLumOfPixel(temp[i]));//
						counter++;
					}
				}
				for(int u=0; u<values.length; u++) {
					result += values[u] * filter[u];
				}
				result /= filterWeight;
				dstPixels[pos] = result;
			}
		}
    }
    
    void showDstPixels(int[] filteredPixels, int[] dstPixels) {
    	for(int i=0; i<dstPixels.length; i++) {
    		int lum = (int) (0.299*filteredPixels[i] + 0.587*filteredPixels[i] + 0.114*filteredPixels[i] + parameter1);
			lum = Math.min(lum,255);
			dstPixels[i] = 0xFF000000 | (lum<<16) | (lum<<8) | lum;
    	}
    }
    
    void showFilteredPixelsOfJoinedConvolutions(int[] pixelsFilterA, int[] pixelsFilterB, int[] dstPixels) {
    	if(pixelsFilterA.length != pixelsFilterB.length) return;
    	int a,b;
    	for(int i = 0; i < pixelsFilterA.length; i++) {
    		a = (pixelsFilterA[i]>>8)&0xFF;
    		b = (pixelsFilterB[i]>>8)&0xFF;
    		int result = (a+b)/2;
    		int lum = (int) (0.299*result + 0.587*result + 0.114*result);
    		dstPixels[i] = 0xFF000000 | (lum<<16) | (lum<<8) | lum;
    	}
    }
    
    void showMagnitudeOfJoinedConvolutions(int[] pixelsFilterA, int[] pixelsFilterB, int[] dstPixels) {
    	if(pixelsFilterA.length != pixelsFilterB.length) return;
    	int a,b;
    	for(int i = 0; i < pixelsFilterA.length; i++) {
    		a = pixelsFilterA[i];
    		b = pixelsFilterB[i];
    		int result = (int) Math.sqrt((a*a+b*b));
    		int lum = (int) (0.299*result + 0.587*result + 0.114*result + parameter1);
    		dstPixels[i] = 0xFF000000 | (lum<<16) | (lum<<8) | lum;
    	}
    }
    
    void showAngleOfJoinedConvolutions(int[] pixelsFilterA, int[] pixelsFilterB, int[] dstPixels) {
    	if(pixelsFilterA.length != pixelsFilterB.length) return;
    	int a,b;
    	for(int i = 0; i < pixelsFilterA.length; i++) {
    		a = pixelsFilterA[i];
    		b = pixelsFilterB[i];
    		double tempResult = Math.atan2(b, a) / Math.PI;
    		int result = (int) (tempResult * 128 + 128);
    		int lum = (int) (0.299*result + 0.587*result + 0.114*result + parameter1);
    		dstPixels[i] = 0xFF000000 | (lum<<16) | (lum<<8) | lum;
    	}
    }
    
    void showColoredPixelsOfJoinedConvolutions(int[] pixelsFilterA, int[] pixelsFilterB, int[] dstPixels) {
    	if(pixelsFilterA.length != pixelsFilterB.length) return;
    	int a,b;
    	for(int i = 0; i < pixelsFilterA.length; i++) {
    		a = pixelsFilterA[i];
    		b = pixelsFilterB[i];
    		float angle = (float) (Math.atan2(b, a) / Math.PI);
    		float magnitude = (float) (Math.sqrt(a*a+b*b) / 128);
    		Color color = Color.getHSBColor(angle, magnitude, magnitude);
    		int rgbVal = color.getRGB();
    		dstPixels[i] = 0xFF000000 | (rgbVal<<16) | (rgbVal<<8) | rgbVal;
    	}
    }
    
    float getFilterWeight(float[] filter) {
    	float filterWeight = 0.0f;
    	for(int f=0; f<filter.length; f++) {
    		filterWeight += Math.abs(filter[f]);
    	}
    	return filterWeight;
    }
    
    int getLumOfPixel(int rgbValue) {
		int r = (rgbValue>>16)&0xFF;
		int g = (rgbValue>> 8)&0xFF;
		int b = (rgbValue    )&0xFF;
		int lum = (int) (0.299*r + 0.587*g + 0.114*b);
		return lum;
    }
    

}
    
