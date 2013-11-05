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
	private ImageView dstView;			// scaled image view
	
	private JComboBox methodList;		// the selected scaling method
	private JLabel statusLine;			// to print some status text
	private JTextField parameterInput1;		// to input a scaling factor
	private double parameter1 = 1;		// initial scaling factor

	public Process() {
        super(new BorderLayout(border, border));
        
        // load the default image
        File input = new File("360411_pixelio.png");
        
        if(!input.canRead()) input = openFile(); // file not found, choose another image
        
        srcView = new ImageView(input);
        srcView.setMaxSize(new Dimension(maxWidth, maxHeight));
       
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
        		"Y-Gradient", "X-Gradient Sobel", "Y-Gradient Sobel", 
        		"Gradientenbetrag", "Gradientenwinkel", "Gradientenwinkel Farbe", "Kombination"};
        
        methodList = new JComboBox(methodNames);
        methodList.setSelectedIndex(0);		// set initial method
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
        images.add(dstView);
        
        add(controls, BorderLayout.NORTH);
        add(images, BorderLayout.CENTER);
        add(statusLine, BorderLayout.SOUTH);
               
        setBorder(BorderFactory.createEmptyBorder(border,border,border,border));
        
        // perform the initial scaling
        processImage(true);
	}
	
	private File openFile() {
        JFileChooser chooser = new JFileChooser();
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
    	
    	String message = "\"" + methodName + "\"";

    	statusLine.setText(message);
    	int filterWidth;
    	int filterHeight;
		long startTime = System.currentTimeMillis();
		//{"Kopie", "Graustufen", "X-Gradient", "Y-Gradient", 
		//"X-Gradient Sobel", "Y-Gradient Sobel", "Gradientenbetrag", 
		//"Gradientenwinkel", "Gradientenwinkel Farbe", "Kombination"}
    	switch(methodList.getSelectedIndex()) {
    	case 0:	// Kopie
    		doCopy(srcPixels, dstPixels, width, height);
    		break;
    	case 1:	// Graustufen
    		doGray(srcPixels, dstPixels, width, height);
    		break;
    	case 2:	// X-Gradient
//    		int temp[] = new int[width * height];
//    		doGray(srcPixels, temp, width, height);
    		doSimpleHorizontalConvolution(srcPixels, dstPixels, width, height);
    		break;
    	case 3:	// Y-Gradient
    		float[] yGradientFilter = {-0.5f, 0, 0.5f};
    		filterWidth = 1;
    		filterHeight = 3;
    		doConvolution(srcPixels, dstPixels, width, height, yGradientFilter, filterWidth, filterHeight);
    		break;
    	case 4:	// "X-Gradient Sobel
    		float[] sobelFilterX = {-1, 0, 1,
    								-2, 0, 2,
    								-1, 0, 1};
    		filterWidth = 3;
    		filterHeight = 3;
    		doConvolution(srcPixels, dstPixels, width, height, sobelFilterX, filterWidth, filterHeight);
    		break;
    	case 5:	// Y-Gradient Sobel
    		float[] sobelFilterY = {-1, -2, -1,
									0, 0, 0,
									1, 2, 1};
    		filterWidth = 3;
    		filterHeight = 3;
    		doConvolution(srcPixels, dstPixels, width, height, sobelFilterY, filterWidth, filterHeight);
    		break;
    	case 6:	// Gradientenbetrag
    		
    		break;
    	case 7:	// Gradientenwinkel
    		
    		break;
    	case 8:	// Gradientenwinkel Farbe
    		
    		break;
    	case 9:	// Kombination
    		
    		break;
    	default:	
    		break;
    	}

		long time = System.currentTimeMillis() - startTime;
		   	
        dstView.setPixels(dstPixels, width, height);
        
        frame.pack();
        
    	statusLine.setText(message + " in " + time + " ms");
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
    
    void doSimpleHorizontalConvolution(int srcPixels[], int dstPixels[], int width, int height) {
    	float[] filter = {-0.5f, 0, 0.5f};
    	for (int y = 0; y < height; y++) {
			
			for (int x = 0; x < width; x++) {
				int pos	= y * width + x;
				int left = (pos-1 < 0 || x == 0) ? (srcPixels[pos]>>16)&0xFF : (srcPixels[pos-1]>>16)&0xFF;
				int middle = srcPixels[pos];
				int right = (pos+1 >= srcPixels.length || x == width - 1) ? (srcPixels[pos]>>16)&0xFF : (srcPixels[pos+1]>>16)&0xFF; 
				
				int result = (int) (left * filter[0] + middle * filter[1] + right * filter[2]);
				
//				int r = ((left>>16)&0xFF * filter[0] + (middle>>16)&0xFF * filter[1] + (right>>16)&0xFF * filter[2]) / 2;
//				int g = ((left>> 8)&0xFF * filter[0] + (middle>> 8)&0xFF * filter[1] + (right>> 0)&0xFF * filter[2]) / 2;
//				int b = ((left    )&0xFF * filter[0] + (middle    )&0xFF * filter[1] + (right    )&0xFF * filter[2]) / 2;
				
//				int r = (result>>16)&0xFF;
//				int g = (result>> 8)&0xFF;
//				int b = (result    )&0xFF;
				
				int lum = (int) (0.299*result + 0.587*result + 0.114*result + parameter1);
				//System.out.println(lum);
//				System.out.println("before conv:" + srcPixels[pos]);
//				System.out.println("after conv:" + result);
				//lum = Math.min(lum,255);
				dstPixels[pos] = 0xFF000000 | (lum<<16) | (lum<<8) | lum;
				//dstPixels[pos] = result;
				
			}
		}
    }
    
    void doSimpleVerticalConvolution(int srcPixels[], int dstPixels[], int width, int height) {
    	
    }
    
    void doConvolution(int srcPixels[], int dstPixels[], int width, int height, float filter[], int filterWidth, int filterHeight) {
    	for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pos = y * width + x;
				int counter = 0;
				int result = 0;
				int[] values = new int[filter.length];
				for(int j=pos-(filterHeight/2)*width; j<=pos+(filterHeight/2)*width; j = j+width) {
					for(int i=j-(filterWidth/2); i<=j+(filterWidth/2); i++)
					{
						if(i<0 || i>=srcPixels.length || (i != 0 && pos % width == 0 && (i+1) % width == 0) || (pos != 0 && (pos+1) % width == 0 && i % width == 0)) 
							values[counter] = (srcPixels[pos]>>16)&0xFF;
						else 
							values[counter] = (srcPixels[i]>>16)&0xFF;
						counter++;
					}
				}
				for(int u=0; u<values.length; u++) {
					result += values[u] * filter[u];
				}
//				int r = (result>>16)&0xFF;
//				int g = (result>> 8)&0xFF;
//				int b = (result    )&0xFF;
				int lum = (int) (0.299*result + 0.587*result + 0.114*result + parameter1);
				dstPixels[pos] = 0xFF000000 | (lum<<16) | (lum<<8) | lum;
			}
		}
    }
    
    int getLumOfPixel(int rgbValue) {
		int r = (rgbValue>>16)&0xFF;
		int g = (rgbValue>> 8)&0xFF;
		int b = (rgbValue    )&0xFF;
		int lum = (int) (0.299*r + 0.587*g + 0.114*b);
		return lum;
    }
    

}
    
