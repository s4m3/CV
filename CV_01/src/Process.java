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
    		
    		break;
    	case 3:	// Y-Gradient
    		
    		break;
    	case 4:	// "X-Gradient Sobel
    		
    		break;
    	case 5:	// Y-Gradient Sobel
    		
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
    

}
    
