/** This is an AWT widget that was developed for use in the ochre applet.
Has great potential for use elsewhere. Graphically links a matrix to an
icon, by depicting the values contained in the double[][] matrix
(constrained to values between 0 and 1) by drawing them as squares lit up
within an icon. Options include highlighting diagonal positions within a
square matrix, and allowing the user to alter the widget by manipulating
the mouse or by applying a simple filtering algorithm to the widget. */

import java.awt.*;

public class IconMatrix extends Canvas {

  public static final double[][] BLUR={{1,2,1},{2,4,2},{1,2,1}};
  public static final double[][] SHARPEN={{-1,-1,-1},{-1,9,-1},{-1,-1,-1}};

  private int m,n,hCellSize,vCellSize;
  private double CellValue[][];
  private boolean Drawable;
  private boolean rightclick;
  private boolean diagonal;
  private float hue, sat;

/** The default constructor. Creates an IconMatrix with 16 rows, 12 columns,
composed of individual squares 4x4 pixels in area. The color displayed is
pastel green, and the widget will allow drawing and filtering.
@return IconMatrix object
*/
  public IconMatrix() {
    this(16,12,4,4,.4f,.6f,true,false);
  }

/** The main constructor. Size of the entire widget is determined by the
size (in pixels) specified for individual squares, and by the number of
squares.
@param tm Number of rows in the IconMatrix.
@param tn Number of columns.
@param th Width of individual elements (in pixels).
@param tv Height of individual elements (in pixels).
@param thue HSB color value for hue
@param tsat HSB color value for saturation
@param td boolean specifying whether the widget allows drawing and filtering
@param tdiag boolean specifying whether the widget will highlight diagonal elements.
@return IconMatrix object
*/
  public IconMatrix(int tm, int tn, int th, int tv,
                      float thue, float tsat, boolean td, boolean tdiag) {
    super();
    m=tm;
    n=tn;
    hCellSize=th;
    vCellSize=tv;
    hue=thue;
    sat=tsat;
    Drawable=td;
    diagonal=tdiag;
    CellValue=new double[m][n];

    for (int i=0;i<m;i++) {
      for (int j=0;j<n;j++) {
        CellValue[i][j]=0;
      }
    }
    reshape(0,0,hCellSize*n,vCellSize*m);
  }
                                      
/** Sets an individual matrix element to a specified value. Does not call repaint().
@param i Row of element
@param j Column of element
@param value New value for element
*/
  public void setCell(int i,int j, double value) {
    CellValue[i][j]=value;
//    repaint();
  }

/** Used for reading a specified element.
@param i Row of element
@param j Column of element
@return Value of specified element
*/
  public double getCell(int i,int j) {
    return CellValue[i][j];
  }

/** Sets the entire IconMatrix to its argument, and then invokes repaint().
@param inputMatrix New array of IconMatrix values. Must have the same array
dimensions as the IconMatrix.
*/
  public void setMatrix(double[][] inputMatrix) {
    CellValue=inputMatrix;
    repaint();
  }

/** Used for reading the entire IconMatrix as a 2-D array.
@return A double[][] array containing the matrix values
*/
  public double[][] getMatrix() {
    return CellValue;
  }

/** Sets the entire IconMatrix to its argument, but takes a double[] array
of m*n elements instead of a double[][] array of m rows and n columns.
Assigns values from its 1-D array argument, row by row, from top to bottom.
The assignment is followed by a call to repaint().
@param inputVector New values for the IconMatrix elements. Must be a double[]
with m*n elements.
*/
  public void setVector (double[] inputVector) {
    // inputVector MUST be a double[] with mxn elements.
    double temp;
    if (inputVector.length == (m*n)) {
      for (int i=0;i<m;i++) {
        for (int j=0;j<n;j++) {
          CellValue[i][j]= inputVector[(n*i)+j];
        }
      }
    }
    else {
      System.out.println ("IconMatrix.setMatrix: Invalid argument");
    }
    repaint();
  }

/** Returns the values contained in the IconMatrix, as a double[] array
of m*n elements instead of a double[][] array of m rows and n columns.
Values are returned in a 1-D array, row by row, from top to bottom.
@return outputVector Values of the IconMatrix elements, in the format of a
double[] with m*n elements.
*/
  public double[] getVector() {
    double[] outputVector = new double[m*n];
    for (int i=0;i<m;i++) {
      for (int j=0;j<n;j++) {
        outputVector[(n*i)+j] = CellValue[i][j];
      }
    }
    return outputVector;
  }

/**
Draws the IconMatrix AWT widget.
@param g Graphics object.
*/
  public void paint(Graphics g) {
    Color cellShade;
    float bright;
    float diaghue;
    diaghue=hue+0.6f;
    if (diaghue>1) diaghue--;

    for (int i=0;i<m;i++) {
      for (int j=0;j<n;j++) {
        bright=(float)(CellValue[i][j]);
        if ((i==j) && ((diagonal==true) && (m==n))) {
          cellShade=Color.getHSBColor(diaghue,sat,bright);
        } else {
          cellShade=Color.getHSBColor(hue,sat,bright);
        }
        g.setColor(cellShade);
        g.fill3DRect(j*hCellSize,i*vCellSize,hCellSize,vCellSize,true);

      }
    }
  }

/**Updates the IconMatrix AWT widget. This method simply calls paint.
@param g Graphics object
*/
  public void update(Graphics g) { paint(g); }

/**Handles mouseDown Event objects (Java 1.0). If Drawable flag is set, then
when user clicks left mouse button, the square at the current mouse position
has its element set to 1.0. If user clicks middle/right mouse button, the
element is set to 0.0.
@param e Event object
@param x x-coordinate of mouse position within the IconMatrix AWT widget
@param y y-coordinate of mouse position
@return a boolean value signifying whether the mouse event was handled by
the widget.
*/
  public boolean mouseDown (Event e, int x, int y) {
    if ((e.modifiers & Event.META_MASK) != 0) {
        //clicked right mouse button
        rightclick=true;
        }
    else {
      if ((e.modifiers & Event.ALT_MASK) !=0) {
      //clicked middle mouse button
        rightclick=true;
        }
      else {
        //clicked left mouse button
        rightclick=false;
      }
    }

    if (Drawable) mousetrap(x,y);
    return Drawable;
  }

/**Handles mouseDrag Event objects (Java 1.0). If Drawable flag is set,
then when user drags mouse with left mouse button depressed, the square
at the current mouse position has its element set to 1.0. If middle/right
mouse button is depressed, the element is set to 0.0.
@param e Event object
@param x x-coordinate of mouse position within the IconMatrix AWT widget
@param y y-coordinate of mouse position
@return a boolean value signifying whether the mouse event was handled by
the widget.
*/

  public boolean mouseDrag (Event e, int x, int y) {
    if (Drawable) mousetrap(x,y);
    return Drawable;
  }

/** Sets all elements of the IconMatrix to zero, and then invokes repaint().
*/
  public void clear() {
    for (int i=0;i<m;i++) {
      for (int j=0;j<n;j++) {
        CellValue[i][j]=0;
      }
    }
    repaint();
  }


/**Handles keyDown Event objects (Java 1.0). If Drawable flag is set, then
the IconMatrix will handle keyboard events.
<br>The "b" key invokes filter() with a BLUR argument.
<br>The "s" key invokes filter() with a SHARPEN argument.
<br>The "c" key invokes clear().
<br>The capital "P" key dumps the contents of the IconMatrix to standard
output (with <tt>System.out.println()</tt>) in a format that can be read
by an application later.
@param e Event object
@param key An integer specifying which key was pressed.
@return a boolean value signifying whether the keyboard event was handled by
the widget.
*/

  public boolean keyDown(Event e, int key) {
    if (Drawable) {
      if((e.id==Event.KEY_PRESS) && (key == 'b')) {
        filter(BLUR);
      }
      if((e.id==Event.KEY_PRESS) && (key == 's')) {
        filter(SHARPEN);
      }
      if((e.id==Event.KEY_PRESS) && (key == 'c')) {
        clear();
      }
      if((e.id==Event.KEY_PRESS) && (key == 'P')) {
        System.out.println("symbol");
        for (int i=0;i<m;i++) {
          for (int j=0;j<n;j++) {
            System.out.println(i+" "+j+" "+CellValue[i][j]);
          }
        }
      }
    }
    return Drawable;
  }

/**Applies a simple filtering operation to the IconMatrix, then calls
repaint().
@param mask A mask filter to be applied to the IconMatrix. Must be a 3x3
double[][] array. Predefined constants for this argument include BLUR
and SHARPEN.
*/
  public void filter(double[][] mask) {
    double[][] swapMatrix=new double[m][n];
    int masksize=3;
    int maskcenter=((masksize-1)/2);
    double masktally;
    for (int i=0;i<m;i++) {
      for (int j=0;j<n;j++) {
        swapMatrix[i][j]=0;
        masktally=0;
        for (int u=0;u<masksize;u++) {
          for (int v=0;v<masksize;v++) {
            if ((i+(u-maskcenter)>=0)&&(i+(u-maskcenter)<m) ) {
              if ((j+(v-maskcenter)>=0)&&(j+(v-maskcenter)<n) ) {
                masktally += mask[u][v];
                swapMatrix[i][j] += mask[u][v]*CellValue[i+u-maskcenter][j+v-maskcenter];
              }
            }
          }
        }
        swapMatrix[i][j] /= masktally;
      }
    }
    CellValue=swapMatrix;
    normalize();
    repaint();
  }

//------------------------------------------------------------------------
//------------- END OF PUBLIC API ----- PRIVATE METHODS BELOW ------------
//------------------------------------------------------------------------
  private void normalize() {
    double min=1;
    double max=0;
    for (int i=0;i<m;i++) {
      for (int j=0;j<n;j++) {

        if (CellValue[i][j]<min) {
          min=CellValue[i][j];
        }

        if (CellValue[i][j]>max) {
          max=CellValue[i][j];
        }

      }
    }
    if (min<max) {
      for (int i=0;i<m;i++) {
        for (int j=0;j<n;j++) {
          CellValue[i][j] -= min;
          CellValue[i][j] /= (max-min);
        }
      }
    }
  }

//------------------------------------------------------------------------
  private boolean mousetrap (int x, int y) {
  //Entry to this subroutine means that a valid mouse event is to be handled
  //at coords (x,y), with rightclick telling which action to take.
  //Drawable has already been checked.
    Graphics g=this.getGraphics();
    float bright;
    double hcell=(double)(x/hCellSize);
    double vcell=(double)(y/vCellSize);
    int j=(int)Math.floor(hcell);
    int i=(int)Math.floor(vcell);
    try {
      if (rightclick==true) {
        CellValue[i][j]=0.;
      }
      else {
        CellValue[i][j]=1.;
      }
      bright=(float)(CellValue[i][j]);
      g.setColor(Color.getHSBColor(hue,sat,bright));
      g.fill3DRect(j*hCellSize,i*vCellSize,hCellSize,vCellSize,true);
    }
    catch (java.lang.ArrayIndexOutOfBoundsException e) {return false;}
    return true;
  }
}
