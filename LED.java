/** LED is an AWT widget, written for use in the ochre applet.
It simulates a rectangular LED (no, not the seven segment kind) which
displays information via brightness.*/

import java.awt.*;

public class LED extends Canvas {
  private int boundwidth, boundheight;
  private int viswidth, visheight;
  private float hue, sat, bright;

/** The default constructor. Creates a 40x40 LED widget, with a "visible"
dimension of 40x20 centered in the middle. The color is red, with 90%
saturation.
@returns LED object 
*/
  public LED() {
    this(40,40,40,20,0.01f,0.9f);
  }

/**The main constructor.
@param tboundwidth The width (in pixels) of the widget.
@param tboundheight The height (in pixels) of the widget.
@param tviswidth The width (in pixels) of the actual light.
@param tvisheight The height (in pixels) of the actual light.
@param thue The hue of the LED. Ranges from 0.0 (red) to 1.0 (violet)
@param tsat The saturation of the LED. Ranges from 0.0 (grayish) to 1.0
(fully saturated).
@return LED object with the given properties.
*/
  public LED(int tboundwidth, int tboundheight,
                 int tviswidth, int tvisheight, float thue, float tsat) {

    super();
    boundwidth=tboundwidth;
    boundheight=tboundheight;
    viswidth=tviswidth;
    visheight=tvisheight;
    hue=thue;
    sat=tsat;
    bright=0;
    reshape(0,0,boundwidth,boundheight);
  }

/**Sets the brightness of the LED.
@param tbright Brightness, ranging from 0.0 to 1.0.
*/
  public void setLED(double tbright) {
    bright=(float)tbright;
    repaint();
  }

/**Paints the LED AWT widget.
@param g Graphics object
*/
  public void paint(Graphics g) {
    g.setColor(Color.getHSBColor(hue,sat,bright));
    g.fill3DRect(((boundwidth-viswidth)/2),((boundheight-visheight)/2),
                       viswidth,visheight,true);
  }

/**Updates the LED AWT widget. This method simply calls paint.
@param g Graphics object
*/
  public void update(Graphics g) { paint(g); }
}
