/**ochre - Optical CHaracter REcognition applet
@author <a href=mailto:tiscione@hhs.net>Jason Tiscione</a>*/
import java.applet.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class ochre extends Applet implements Runnable {

  Thread train;
  Panel upperPanel,lowerPanel,insetPanel;
  Label title_label,subtitle_label,author_label;
  Label S1_label,S2_label,epoch_label,error_label,network_output_label;
  TextField S1_field,S2_field,epoch_field,error_field;
  Button symbolSetTest[];   // The buttons above the IconMatrix objects
  IconMatrix symbolSet[];   // The IconMatrix boxes.
  LED symbolSetLight[];     // The lights below the IconMatrix objects
  BackProp network;         // The neural network.
  String digitfile;
  URL digitURL;
  IconMatrix user_matrix;
  Button blur_button,sharpen_button,test_button,clear_button;
  Button start_training,stop_training,reset_network,reset_inputs;
  IconMatrix network_output;
  double[][] targets;
  boolean training_status = false;
  int R,S1,S2,S3,NSymbols;

/**Sets up the applet. Reads the applet parameters, reads the symbol form
file through a URL and initializes the network targets matrix, sets up the
layout manager and AWT components, and initializes a new BackProp object.
*/
  public void init() {

    /* Read HTML applet parameters */
    digitfile = this.getParameter("digitfile");
    try { R = Integer.parseInt(this.getParameter("inputs")); }
    catch (java.lang.NumberFormatException e) { R=192; }
    try { S1 = Integer.parseInt(this.getParameter("S1")); }
    catch (java.lang.NumberFormatException e) { S1=8; }
    try { S2 = Integer.parseInt(this.getParameter("S2")); }
    catch (java.lang.NumberFormatException e) { S2=12; }
    try { S3 = Integer.parseInt(this.getParameter("S3")); }
    catch (java.lang.NumberFormatException e) { S3=10; }
    NSymbols = S3;

    /* Establish targets matrix */
    targets = new double[S3][NSymbols];
    for (int i=0;i<S3;i++) {
      for (int j=0;j<NSymbols;j++) {
        if (i==j) {
          targets[i][j]=1;
        } else {
          targets[i][j]=0;
        }
      }
    }

    /* Set up our panels and LayoutManagers */
    /* Applet container's layout is GridLayout */
    /* Each panel uses a GridBagLayout */

    setLayout(new GridLayout(0,1,0,0));
    GridBagLayout ugb = new GridBagLayout();
    GridBagLayout lgb = new GridBagLayout();
    GridBagLayout ingb = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    upperPanel = new Panel();
    lowerPanel = new Panel();
    insetPanel = new Panel();
    upperPanel.setLayout(ugb);
    lowerPanel.setLayout(lgb);
    insetPanel.setLayout(ingb);

    /* Initialize a new BackProp object */
    network = new BackProp (R,S1,S2,S3);

    /* Initialize the AWT object arrays */
    symbolSetTest = new Button[NSymbols];
    symbolSet = new IconMatrix[NSymbols];
    symbolSetLight = new LED[NSymbols];

    /* Initialize each element of each array */
    for (int i=0;i<NSymbols;i++) {
      symbolSetTest[i] = new Button("test"+i);
      symbolSet[i]=new IconMatrix();
      symbolSetLight[i] = new LED();
    }
    /* Form URL for symbol form file */
    try{digitURL=new URL(this.getCodeBase(),digitfile);}
    catch(MalformedURLException e) {
      System.out.println("Error-MalformedURLException");
    }
    /* Load the symbol forms from digitURL into symbolSet objects */
    try { ReadSymbolFile(digitURL); }
    catch (java.io.IOException e) {
      System.out.println("IO Exception... file error");
    }

    /* Initialize the individual AWT Components */
    title_label = new Label("O C H R E",Label.CENTER);
    title_label.setFont(new Font("Helvetica", Font.BOLD, 16));
    subtitle_label = new Label("A Backpropagation Neural Network Demo Applet",
                                                          Label.CENTER);
    author_label = new Label("by Jason Tiscione",Label.CENTER);
    author_label.setForeground(Color.white);
    author_label.setFont(new Font("Helvetica", Font.ITALIC, 12));
    S1_label = new Label(" input neurons:");
    S2_label = new Label("hidden neurons:");
    epoch_label = new Label("  training epochs:");
    error_label = new Label("sum squared error:");
    S1_field = new TextField(Integer.toString(S1),3);
    S2_field = new TextField(Integer.toString(S2),3);
    error_field = new TextField(12);
    epoch_field = new TextField("0",4);
    blur_button = new Button("blur");
    sharpen_button = new Button("sharpen");
    test_button = new Button("test");
    clear_button = new Button("clear");
    user_matrix = new IconMatrix (16,12,8,8,.1f,.6f,true,false);
    start_training = new Button("Start training");
    stop_training = new Button("Stop training");
    reset_network = new Button("Reset network");
    reset_inputs = new Button("Reset inputs");
    network_output = new IconMatrix (10,10,6,6,.01f,.8f,false,true);
    network_output_label = new Label("network output");

    /* Set background to ochre hues, font to plain 12pt Helvetica */
    setFont(new Font("Helvetica", Font.PLAIN, 12));
    upperPanel.setBackground(Color.getHSBColor(0.1f,0.4f,0.5f));
    lowerPanel.setBackground(Color.getHSBColor(0.1f,0.4f,0.25f));
    insetPanel.setBackground(Color.getHSBColor(0.1f,0.4f,0.5f));

    /* Set error_field and epoch_field to read-only */
    error_field.setEditable(false);
    epoch_field.setEditable(false);

    /* Lay all AWT components out onto their container panels */
    add(upperPanel);
    add(lowerPanel);
    try {
     /* Add title and subtitle label to upperPanel */
      addComponent(upperPanel,title_label,0,0,10,1,
          GridBagConstraints.NONE, GridBagConstraints.CENTER,0,0,0,0);
      addComponent(upperPanel,subtitle_label,0,1,10,1,
          GridBagConstraints.NONE, GridBagConstraints.CENTER,0,0,0,0);
     /* Add row of test buttons to upperPanel */
      for (int i=0;i<10;i++) {
        addComponent(upperPanel,symbolSetTest[i],i,2,1,1,
          GridBagConstraints.NONE, GridBagConstraints.CENTER,10,10,5,5);
      }
     /* Add IconMatrix objects to upperPanel */
      for (int i=0;i<10;i++) {
        addComponent(upperPanel,symbolSet[i],i,3,1,1,
          GridBagConstraints.NONE, GridBagConstraints.CENTER,0,0,5,5);
      }
     /* Add indicator lights to upperPanel */
      for (int i=0;i<10;i++) {
        addComponent(upperPanel,symbolSetLight[i],i,4,1,1,
          GridBagConstraints.NONE, GridBagConstraints.CENTER,0,0,5,5);
      }
     /* Add blur, sharpen, test, and clear buttons to lowerPanel*/
      addComponent(lowerPanel,clear_button,0,0,1,1,
        GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH,0,0,5,5);
      addComponent(lowerPanel,blur_button,0,1,1,1,
        GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER,0,0,5,5);
      addComponent(lowerPanel,sharpen_button,0,2,1,1,
        GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER,0,0,5,5);
      addComponent(lowerPanel,test_button,0,3,1,1,
        GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER,0,0,5,5);
      addComponent(lowerPanel,author_label,0,4,2,1,
        GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH,0,0,5,5);
      addComponent(lowerPanel,user_matrix,1,0,3,4,
        GridBagConstraints.NONE, GridBagConstraints.NORTHWEST,0,0,5,5);
      addComponent(lowerPanel,insetPanel,4,0,
        GridBagConstraints.REMAINDER,GridBagConstraints.REMAINDER,
        GridBagConstraints.BOTH, GridBagConstraints.CENTER,0,0,5,5);
     /* Add start_training, stop_training, Reset_network buttons
         and network_output IconMatrix to insetPanel */
      addComponent(insetPanel,start_training,0,0,1,1,
        GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER,10,10,5,5);
      addComponent(insetPanel,stop_training,0,1,1,1,
        GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER,10,10,5,5);
      addComponent(insetPanel,reset_network,0,2,1,1,
        GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER,10,10,5,5);
      addComponent(insetPanel,reset_inputs,0,3,1,1,
        GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH,10,10,5,5);
     /* Add labels and textfields to insetlabel */
      addComponent(insetPanel,epoch_label,1,0,1,1,
        GridBagConstraints.NONE, GridBagConstraints.CENTER,0,0,0,0);
      addComponent(insetPanel,error_label,1,1,1,1,
        GridBagConstraints.NONE, GridBagConstraints.CENTER,0,0,0,0);
      addComponent(insetPanel,S1_label,1,2,1,1,
        GridBagConstraints.NONE, GridBagConstraints.CENTER,0,0,0,0);
      addComponent(insetPanel,S2_label,1,3,1,1,
        GridBagConstraints.NONE, GridBagConstraints.CENTER,0,0,0,0);
      addComponent(insetPanel,epoch_field,2,0,1,1,
        GridBagConstraints.NONE, GridBagConstraints.CENTER,0,0,0,0);
      addComponent(insetPanel,error_field,2,1,1,1,
        GridBagConstraints.NONE, GridBagConstraints.CENTER,0,0,0,0);
      addComponent(insetPanel,S1_field,2,2,1,1,
        GridBagConstraints.NONE, GridBagConstraints.CENTER,0,0,0,0);
      addComponent(insetPanel,S2_field,2,3,1,1,
        GridBagConstraints.NONE, GridBagConstraints.CENTER,0,0,0,0);
     /* add network_output icon to insetPanel */
      addComponent(insetPanel,network_output,4,0,2,2,
        GridBagConstraints.NONE, GridBagConstraints.SOUTH,0,0,5,5);
      addComponent(insetPanel,network_output_label,4,2,1,1,
        GridBagConstraints.NONE, GridBagConstraints.NORTH,0,0,0,0);
    } catch (AWTException e) {
      System.out.println("AWTException");
      e.printStackTrace();
    }
  }
//--------------------------------------------------------------------
/** Draws the applet. Invokes repaint() method of all IconMatrix and
LED objects.
@param g Graphics object.
*/
  public void paint (Graphics g){
    for (int i=0;i<NSymbols;i++){
      symbolSet[i].repaint();
      symbolSetLight[i].repaint();
    }
    user_matrix.repaint();
    network_output.repaint();
  }
//--------------------------------------------------------------------
  private static void addComponent(Container container, Component component,
      int gridx, int gridy, int gridwidth, int gridheight, int fill,
      int anchor, int ipadx, int ipady, int extpadx, int extpady)
      throws AWTException {
    LayoutManager lm = container.getLayout();
    if(!(lm instanceof GridBagLayout)) {
      throw new AWTException ("Invalid layout" + lm);
    } else {
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = gridx;
      gbc.gridy = gridy;
      gbc.gridwidth = gridwidth;
      gbc.gridheight = gridheight;
      gbc.fill = fill;
      gbc.anchor = anchor;
      gbc.ipadx = ipadx;
      gbc.ipady = ipady;
      gbc.insets = new Insets(extpady,extpadx,extpady,extpadx);
      ((GridBagLayout)lm).setConstraints(component, gbc);
      container.add(component);
    }
  }
//--------------------------------------------------------------------
/** Handles "action" Event objects (Java 1.0) generated when user clicks
on a button. Calls the appropriate method for the button.
@param e the Event object
@param arg the Event argument, not used explicitly by this method
@return boolean signifying whether the action event was handled
*/
  public boolean action (Event e, Object arg) {
    int targetButtonIndex=-1;
    if (e.id==Event.ACTION_EVENT) {
      for (int i=0;i<NSymbols;i++) {
        if (e.target==symbolSetTest[i]) {
          targetButtonIndex=i;
        }
      }
      if (targetButtonIndex != -1) {
      testOnePattern(symbolSet[targetButtonIndex]);
      return true;
      }
      else {
        if (e.target==test_button) {
          testOnePattern(user_matrix);
        }
        if (e.target==blur_button) {
          user_matrix.filter(IconMatrix.BLUR);
        }
        if (e.target==sharpen_button) {
          user_matrix.filter(IconMatrix.SHARPEN);
        }
        if (e.target==clear_button) {
          user_matrix.clear();
        }
        if (e.target==start_training){
          double[] singlePattern = new double[R];
          double[][] inputPatterns = new double[R][NSymbols];
          for (int j=0;j<NSymbols;j++) {
            singlePattern = symbolSet[j].getVector();
            for (int i=0;i<R;i++) {
              inputPatterns[i][j] = singlePattern[i];
            }
          }
          training_status=network.trainInit(inputPatterns,targets);
        }
        if (e.target==stop_training){
          training_status=false;
        }
        if (e.target==reset_network) {
          int new_S1,new_S2,old_S1,old_S2;
          old_S1=S1;
          old_S2=S2;
          try {
            new_S1=Integer.parseInt(S1_field.getText());
            new_S2=Integer.parseInt(S2_field.getText());
          }
          catch (java.lang.NumberFormatException exc) {
            new_S1=old_S1;
            new_S2=old_S2;
          }
          S1_field.setText(Integer.toString(new_S1));
          S2_field.setText(Integer.toString(new_S2));
          S1=new_S1;
          S2=new_S2;
          network = new BackProp(R,S1,S2,S3);
          epoch_field.setText("0");
          error_field.setText("");
          testAllPatterns();
         }
        if (e.target==reset_inputs) {
          try { ReadSymbolFile(digitURL); }
          catch (java.io.IOException ex) {
            System.out.println("IO Exception... file error");
          }
          testAllPatterns();
          error_field.setText("");
          paint(this.getGraphics());             
        }
      }
    }
    return true;
  }

//--------------------------------------------------------------------
/**Starts the applet. Tests all the inputs on the network, and fires a new
Thread object (train) which will be doing the training.
*/
  public void start() {
    testAllPatterns();
    train = new Thread(this);
    train.start();
  }
//--------------------------------------------------------------------
/**Stops the applet. Calls stop() method of Thread train.
*/
  public void stop() {
    train.stop();
  }
//--------------------------------------------------------------------
/**Invoked by Thread train, this method is responsible for training the
network. Runs in a loop, repeatedly checking training_status flag. If the
flag is true, the method updates the applet display (epochs, sum squared
error display, and the network output icon) and in the process, it invokes
network.trainNet().
*/
  public void run() {
    String errorval;
    while(true){
      if (training_status == false) {
        try {
          Thread.sleep((int)(100));
        }  catch (InterruptedException e) {
        break;
        }
      } else {
        epoch_field.setText(Integer.toString(network.getepoch()));
        errorval=Float.toString((float)network.trainNet());
        error_field.setText(errorval);
        testAllPatterns();
      }
    }
  }
//--------------------------------------------------------------------
  private void testOnePattern(IconMatrix symbol){
    double[] inputVector = symbol.getVector();
    double[][] result=network.testNet(inputVector);
    vectorLight(result);
  }
//--------------------------------------------------------------------
  private void vectorLight(double[][] outputVector){
  //Although double[][], outputVector should have only one column.
    for(int i=0;i<S3;i++){
      symbolSetLight[i].setLED(outputVector[i][0]);
    }
}
//--------------------------------------------------------------------
  private void testAllPatterns() {
    double[] singlePattern = new double[R];
    double[][] inputPatterns = new double[R][NSymbols];
    for (int j=0;j<NSymbols;j++) {
      singlePattern = symbolSet[j].getVector();
      for (int i=0;i<R;i++){
        inputPatterns[i][j] = singlePattern[i];
      }
    }
    double[][] result = network.testNet(inputPatterns);
    matrixLight(result);
  }
//--------------------------------------------------------------------
  private void matrixLight(double[][] outputMatrix) {
    network_output.setMatrix(outputMatrix);
  }
//-------------------------------------------------------------------
  private void ReadSymbolFile(URL digitURL) throws java.io.IOException {
    double dubval;
    int symbol=-1;
    int token=0;
    int row,col;
    double[] N=new double[4];  //we won't use the zeroth element

    InputStream is;
    StreamTokenizer stkz;

    try {is = digitURL.openStream();}
    catch(java.io.IOException e) {
      System.out.println(digitURL+"- Malformed URL error.");
      return;
    }
    stkz = new StreamTokenizer(is);
    stkz.resetSyntax();
    stkz.eolIsSignificant(true);
    stkz.whitespaceChars(0,' ');
    stkz.wordChars(33,255);
    stkz.parseNumbers();

    while (stkz.nextToken() != stkz.TT_EOF) {
      token++;
/*
      switch (stkz.ttype) {
        case stkz.TT_EOL:
          token=0;
          break;
        case stkz.TT_WORD:

          if (stkz.sval.equalsIgnoreCase("symbol")) {
            if (stkz.nextToken() == stkz.TT_NUMBER) {
              symbol=(int)stkz.nval;
            }
          }
          //Kill off the rest of the line after any word appears...
          do{stkz.nextToken();} while (stkz.ttype!=stkz.TT_EOL);
          token=0;
          break;
        case stkz.TT_NUMBER:
          N[token]=stkz.nval;
          if (token==3) {
            if (symbol != -1) {
              row=(int)N[1];
              col=(int)N[2];
              dubval=N[3];
              symbolSet[symbol].setCell(row,col,dubval);
            }
            //Kill off the rest of the line...
            do{stkz.nextToken();} while (stkz.ttype!=stkz.TT_EOL);
            token=0;
            break;
          }
*/
      if (stkz.ttype==stkz.TT_EOL) token=0;
      if (stkz.ttype==stkz.TT_WORD){

          if (stkz.sval.equalsIgnoreCase("symbol")) {
            if (stkz.nextToken() == stkz.TT_NUMBER) {
              symbol=(int)stkz.nval;
            }
          }
          //Kill off the rest of the line after any word appears...
          do{stkz.nextToken();} while (stkz.ttype!=stkz.TT_EOL);
          token=0;
       }
      if (stkz.ttype==stkz.TT_NUMBER){
          N[token]=stkz.nval;
          if (token==3) {
            if (symbol != -1) {
              row=(int)N[1];
              col=(int)N[2];
              dubval=N[3];
              symbolSet[symbol].setCell(row,col,dubval);
            }
            //Kill off the rest of the line...
            do{stkz.nextToken();} while (stkz.ttype!=stkz.TT_EOL);
            token=0;
          }
      }
    }
    for (int i=0;i<NSymbols;i++) {
      symbolSet[i].repaint();
    }
  }
}
