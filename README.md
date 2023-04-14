Original work from: https://kedras.mif.vu.lt/bastys/academic/ATE/ochre/ochre.html

# Introduction
One of the most typical problems to which a neural network is applied is that of optical character recognition.
Recognizing characters is a problem that at first seems extremely simple- but it's extremely difficult in practice to
program a computer to do it. And yet, automated character recognition is of vital importance in many industries such
as banking and shipping. The U.S. post office uses an automatic scanning system to recognize the digits in ZIP codes.
You may have used scanning software that can take an image of a printed page and generate an ASCII document from it.
These devices work by simulating a type of neural network known as a backpropagation network.


# Backpropagation neural networks
 The algorithm behind backpropagation networks was discovered independently by several research groups at about the
 same time in 1985-86. The "neurons" in these networks are inspired by biological neurons, which are much more complex
 in comparison. The generic connectionist neuron commonly used in most types of neural networks is extremely simple.
 
A backpropagation network usually consists of three (or sometimes fewer) layers of neurons: the input layer, the
hidden layer, and the output layer. In the applet below, the input layer consists of eight neurons, and the hidden
layer contains 12. The size of the output layer is fixed by the applet to ten neurons- because these are the neurons
that are to fire selectively according to which input is presented.

The backpropagation algorithm works by what is known as supervised training. It first submits an input for a forward
pass through the network. The network output is compared to the desired output, which is specified by a "supervisor"
(the computer running the simulation), and the error for all the neurons in the output layer is calculated. The
fundamental idea behind backpropagation is that the error is propagated backward to earlier layers so that a gradient
descent algorithm can be applied. This assumes that the network can "run backwards", meaning that for any two
connected neurons, the "backward weights" must be the same as the "forward weights". This isn't true for real neurons,
and so the backpropagation network is unbiological. Yet, backpropagation is popular because it is well-known to
generate useful results when applied to real-world problems. 

# The source code:

The source code to the applet and the underlying neural network is available below. I tried to keep the
backpropagation algorithm as general as possible- all of the tasks that are specifically related to optical 
character recognition are handled by the applet itself. The applet and the AWT widgets are written in Java 1.0 
for browser compatibility (so don't be surprised if your JDK issues a compile-timeinsult).

BackProp.java- the neural network object.
Note that with the code as it is, you are limited to a three layer network that uses a log-sigmoid transfer function
(i.e., 1/(1+e-x)) for all neurons. If you want to modify the code for your own purposes, feel free to knock yourself
out.

ochre.java- the applet itself.

ochre.txt- file containing definitions of the default symbol set. (Pressing a capital "P" over an icon will dump its
matrix definition to your Java console, if you want to create your own default symbol set files.)

IconMatrix.java- my AWT icon widget that allows drawing and simple image filtering.

LED.java- another AWT widget. Simulates an LED.

# How to work this thing

The ten icons with the digits in them, along the the top of the applet window, are the actual inputs that are used to
test and train the neural network. To submit one to the network, press the "test" button that appears above it. The
row of ten LEDs below the icons indicate the activity of the ten output neurons. When the network is fully trained,
the only active output neuron should be the one below the icon which you just tested. It should glow bright red, while
the others remain black. But on startup, the network is not trained yet- it doesn't "know" anything about what Arabic
numerals look like- and so it won't know how to recognize one. You're likely to see all the neurons light up, at least
a little bit.
To train the network, press the "Start training" button on the lower panel. As the applet begins to train the
network, you will see the number of training epochs (individual training iterations) increase, and the sum
squared error (the cumulative error in the network output) slowly decrease. You will also see the network output
icon changing (at the lower right side of the applet). This icon summarizes all of the network's responses to all 
ten of the training inputs. Squares in the icon that are blue indicate appropriate responses by output neurons,
and those that are red indicate inappropriate responses.

Training should take a few minutes, and is complete after about 150-250 training epochs, when the sum squared error
reaches a low value (approximately 0.01). On my Pentium-133, it usually takes about two minutes of training for the
network to reach an error that low. The network output icon should appear as a diagonal row of bright blue squares. 
At this point, you can press the "Stop training" button to stop training the network. If you press the "test" 
buttons above each digit icon, the appropriate LED should now light up.

You can test the network on your own hand-drawn symbol, by drawing one with your mouse in the large icon at the lower
left of the applet. Your left mouse button will light a pixel up, and your right mouse button will darken it. (If your
mouse only has one button, you can click while holding down your ALT key to darken a pixel.) You can blur or sharpen
your drawing using the buttons along the left of the large icon. To see what the network thinks you just drew a
picture of, press the "test" button at bottom, and see which of the LEDs light up. I have varying luck with it.
Sometimes the network recognizes my digit immediately, and sometimes it refuses to unless the picture is smoothed with
the blur button. One interesting thing to do is to test the network on a novel input, such as a letter "A". Neural
networks behave unpredictably with novel inputs.

The network can be trained on any of your own novel inputs as well. You should be able to alter any of the digit icons
at the top by drawing on it. To blur, sharpen, or clear them, use your "B", "S", and "C" keys respectively. To restore
the original digits, press the "Reset inputs" button. To reset the network itself, press the "Reset network" button.
You can change the number of input layer neurons and hidden layer neurons by changing the numbers in the corresponding 
fields before pressing "Reset network". Too few neurons, and the network will be unable to learn anything.  Too many,
and overlearning becomes likely- the network learns the specific training inputs so well that it won't tolerate any
slight deviation from them.
