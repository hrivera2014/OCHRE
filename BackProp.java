/**BackProp - a backpropagation neural network class 4/98 by
<a href=mailto:tiscione@hhs.net>Jason Tiscione</a>.
<br>Copyright (c) 1998.  All Rights Reserved. You have a non-exclusive,
royalty free, LICENSE to use, modify and redistribute this software
in source and binary code form, provided that 1) this dubious legal
junk at the top appears on all copies of the software; and
ii) you don't use the software in a manner which is "disparaging"
to me- like making fun of the way I program.
This software is provided "AS IS," with no warranty of any kind.
NO MATTER WHAT, you can't sue me for anything. This software is
neither designed nor intended to be used for air traffic control
or for maintaining a nuclear facility, so don't get any crazy ideas.

<br>SUMMARY OF CLASS MEMBER VARIABLES:
<br>int    R = size (# elements) of an input vector accepted by network
<br>int    S1,S2,S3 = number of neurons in input, hidden, output layers
<br>double W1[][],W2[][],W3[][] = weight matrices of input, hidden, output layers
<br>double b1[][],b2[][],b3[][] = bias values of input, hidden, output layers
<br>int    epoch= number of training presentations so far
<br>double lr= learning rate [default 0.01]
<br>double im= learning rate increase [default 1.05]
<br>double dm= learning rate decrease [default 0.7]
<br>double mc= momentum coefficient [default 0.9]
<br>double er= maximum error ratio [default 1.04]
<br> (The following variables are not used until trainInit() is called)
<br>int    Q = number of training inputs
<br>double SSE = sum squared error
<br>double MC = current momentum
<br>double out1[][],out2[][],out3[][] = output from input, hidden, output layers
<br>double d1[][],d2[][],d3[][] = delta log array, for calculating dWn and dbn
<br>double dW1[][],dW2[][],dW3[][] = weight matrix differentials
<br>double db1[][],db2[][],db3[][] = bias matrix differentials
<br>double errors[][] = error matrix
<br>double inputs[][] = training inputs matrix
<br>double targets[][] = training targets matrix (usually an nxn identity matrix).<br>
@author <a href="mailto:tiscione@hhs.net>Jason Tiscione</a>
@version 1.0.  No future revisions planned.
*/

public class BackProp extends Object {
  private int R,S1,S2,S3,epoch;
  private double[][] W1,W2,W3,b1,b2,b3;
  private double lr,im,dm,mc,er;
  int Q; //number of training inputs
  private double SSE; //Sum squared error
  private double MC; //current momentum
  private double[][] out1,out2,out3,errors;
  private double[][] d1,d2,d3,dW1,dW2,dW3,db1,db2,db3;
  private double[][] inputs,targets;

/**Creates a new BackProp object, and initializes its weight & bias arrays.
@param R Size (number of elements) of an input vector accepted by network
@param S1 Number of neurons in input layer
@param S2 Number of neurons in hidden layer
@param S3 Number of neurons in output layer
@param lr Learning rate.
@param im Learning rate increase.
@param dm Learning rate decrease.
@param mc Momentum coefficient
@param er Maximum error ratio
@return BackProp object with specified properties
*/
  public BackProp (int R, int S1, int S2,
                   int S3, double lr, double im,
                   double dm, double mc, double er)
  {
    this.R=R; this.S1=S1; this.S2=S2; this.S3=S3;
    this.lr=lr; this.im=im; this.dm=dm; this.mc=mc; this.er=er;

//Call Nguyen-Widrow random initializer for log-sigmoid neurons.
    InitWB(S1,R,1);
    InitWB(S2,S1,2);
    InitWB(S3,S2,3);
    epoch=0;
  }

/**This constructor simply takes as parameters the number of elements in
each input vector and the number of neurons in the input, hidden, and
output layers. It invokes the main constructor with default values for
its remaining parameters.
@param R Size (# elements) of an input vector accepted by network
@param S1 Number of neurons in input layer
@param S2 Number of neurons in hidden layer
@param S3 Number of neurons in output layer
@return BackProp object with specified properties
*/
  public BackProp (int R, int S1, int S2, int S3) {
    this (R,S1,S2,S3,0.01,1.05,0.7,0.9,1.04);
  }

/**Used to test the network on a single given input. It is merely a
convenience method; this version of testNet() - there are two - takes
a 1-dimensional array (containing a single input vector) as its sole
argument, converts it from double[] to double[][], and passes it to
the other version of testNet (which should preferably be used
instead of this one.) The argument to this method should contain
R elements. It returns a double[][] with S3 rows and one column.
@param inputVector A single input vector, with R elements.
@return Matrix of network outputs.
*/
  public double[][] testNet( double[] inputVector ) {
    double[][] inputarray = new double[R][1];
    for (int i=0;i<R;i++) {
      inputarray[i][0]=inputVector[i];
    }
    return testNet (inputarray);
  }

/**Used to test the network on a given input or inputs. The argument
is an array of inputs. It must have R rows. The number of columns is
arbitrary but must be at least one. This method will return an output
array with S3 rows and the same number of columns that the double[][]
argument has.
<p>Inputs to the network are constrained to the interval [0,1]; to use
this class with inputs that span a different range, a scaling algorithm
would need to be applied to the inputs and targets before calling this
method or trainInit().
<p>The trainInit() method (at least) should be called before invoking
testNet().
@param test_inputs A double[][] matrix of network inputs
@return A double[][] matrix of network outputs
*/
  public double[][] testNet( double[][] test_inputs ) {
    double[][] test_ip,test_out1,test_out2,test_out3;
    test_ip = multiply(W1,test_inputs);
    test_out1 = logsig (test_ip,b1);
    test_ip = multiply(W2,test_out1);
    test_out2 = logsig (test_ip,b2);
    test_ip = multiply(W3,test_out2);
    test_out3 = logsig (test_ip,b3);
    return test_out3;
  }

/**Establishes what the training inputs and targets are to be,
and prepares the network for subsequent calls to trainNet(). This
method need be called only once.
<p>The inputs[][] variable is set to the first argument, and so it must
have R rows and a number of columns which is at least one. The number of
columns establishes Q, the number of inputs. The targets[][] variable is
set to the second argument and so that must have S3 rows and Q columns.
If the number of columns in the two arguments are not the same, or if
the arguments do not have R and S3 rows respectively, the method returns
false. The individual input vectors and corresponding target vectors
used to train the network are thus the column vectors in the inputs[][]
and targets[][] arrays. All neuron activations in this network are
constrained to the interval [0,1] by a log-sigmoid function.
Inputs to the network are also constrained to this interval; to use
this class with a set of inputs that span a different range, a
scaling algorithm would need to be applied to the inputs and targets
before calling this method or testNet().
@param inputs A double[][] matrix of network inputs
@param targets A double[][] matrix of target network outputs
@return A boolean value indicating successful invocation.
*/
  public boolean trainInit( double[][] inputs, double[][] targets ) {
    // First initialize inputs and targets.
    this.inputs = inputs;
    this.targets= targets;

    // First create arrays dW1,dW2,dW3 to be same size as W1,W2,W3 arrays.
    // Java initializes all elements to zero so we don't need to.

    double[][] ip; //used for inner products

    dW1 = new double[S1][R];
    dW2 = new double[S2][S1];
    dW3 = new double[S3][S2];
    db1 = new double[S1][1];
    db2 = new double[S2][1];
    db3 = new double[S3][1];

    MC=0;
    int[] dim_inputs = getSize(inputs);
    int[] dim_targets = getSize(targets);
    Q=dim_inputs[1];
    errors=new double[S3][Q];

    if (dim_targets[1] != Q) {
      System.out.println("TrainNet: Inputs and targets are mismatched.");
      return false;
    }
    if (dim_inputs[0] != R) {
      System.out.println("TrainNet: Inputs do not match net structure.");
      return false;
    }
    if (dim_targets[0] != S3) {
      System.out.println("TrainNet: Targets do not match net structure.");
      return false;
    }
    //PRESENTATION PHASE
    ip = multiply(W1,inputs);
    out1 = logsig (ip,b1);
    ip = multiply(W2,out1);
    out2 = logsig (ip,b2);
    ip = multiply(W3,out2);
    out3 = logsig (ip,b3);

    //Compute errors matrix and sum-squared error (SSE).
    SSE=0;
    for (int i=0;i<S3;i++) {
      for (int j=0;j<Q;j++) {
        errors[i][j] = targets[i][j] - out3[i][j];
        SSE += errors[i][j]*errors[i][j];
      }
    }

    //BACKPROPAGATION PHASE
    d3=deltalog(out3,errors);
    d2=deltalog(out2,d3,W3);
    d1=deltalog(out1,d2,W2);
    return true;
  }

/**Performs ONE iteration (i.e., one epoch) of the backpropagation
algorithm on the network. It updates the weight and bias arrays of
the network, and returns the sum squared error of the network's
output in comparison to the target array.
@return The SSE (sum-sqared error) of the network's output.
*/
  public double trainNet() {
    //Returns SSE after 1 epoch of training on Q input pattern vectors.

    double new_SSE;
    double[][] new_out1,new_out2,new_out3,new_errors;
    double[][] ip; // Used for inner products
    double[][] swap; //Used for swapping references
    new_errors = new double[S3][Q];

    // Create arrays new_W1,new_W2,new_W3
    // and new_b1,new_b2,new_b3.

    double new_W1[][] = new double[S1][R];
    double new_W2[][] = new double[S2][S1];
    double new_W3[][] = new double[S3][S2];
    double new_b1[][] = new double[S1][1];
    double new_b2[][] = new double[S2][1];
    double new_b3[][] = new double[S3][1];

    epoch++;

    // LEARNING PHASE
    // Calculate dW arrays and db arrays
    // dW1:
    for (int i=0;i<S1;i++) {
      for (int j=0;j<R;j++) {
        dW1[i][j] *= MC; // momentum term
        for (int k=0;k<Q;k++) {
          dW1[i][j] += lr * (1-MC) * d1[i][k] * inputs[j][k];
        }
      }
    }
    // db1:
    for (int i=0;i<S1;i++) {
      db1[i][0] *= MC; // momentum term
      for (int k=0;k<Q;k++) {
        db1[i][0] += lr * (1-MC) * d1[i][k];
      }
    }
    // dW2:
    for (int i=0;i<S2;i++) {
      for (int j=0;j<S1;j++) {
        dW2[i][j] *= MC; // momentum term
        for (int k=0;k<Q;k++) {
          dW2[i][j] += lr * (1-MC) * d2[i][k] * out1[j][k];
        }
      }
    }
    // db2:
    for (int i=0;i<S2;i++) {
      db2[i][0] *= MC;
      for (int k=0;k<Q;k++) {
        db2[i][0] += lr * (1-MC) * d2[i][k];
      }
    }
    // dW3:
    for (int i=0;i<S3;i++) {
      for (int j=0;j<S2;j++) {
        dW3[i][j] *= MC; // momentum term
        for (int k=0;k<Q;k++) {
          dW3[i][j] += lr * (1-MC) * d3[i][k] * out2[j][k];
        }
      }
    }
    // db3:
    for (int i=0;i<S3;i++) {
      db3[i][0] *= MC;
      for (int k=0;k<Q;k++) {
        db3[i][0] += lr * (1-MC) * d3[i][k];
      }
    }
    // Add dW and db matrices to W and b to get new_W and new_b
    MC=mc;
    for (int i=0;i<S1;i++) {
      new_b1[i][0] = b1[i][0] + db1[i][0];
      for (int j=0;j<R;j++) {
        new_W1[i][j] = W1[i][j] + dW1[i][j];
      }
    }
    for (int i=0;i<S2;i++) {
      new_b2[i][0] = b2[i][0] + db2[i][0];
      for (int j=0;j<S1;j++) {
        new_W2[i][j] = W2[i][j] + dW2[i][j];
      }
    }
    for (int i=0;i<S3;i++) {
      new_b3[i][0] = b3[i][0] + db3[i][0];
      for (int j=0;j<S2;j++) {
        new_W3[i][j] = W3[i][j] + dW3[i][j];
      }
    }
    // PRESENTATION PHASE
    ip = multiply(new_W1,inputs);
    new_out1 = logsig (ip,new_b1);
    ip = multiply(new_W2,new_out1);
    new_out2 = logsig (ip,new_b2);
    ip = multiply(new_W3,new_out2);
    new_out3 = logsig (ip,new_b3);
    
    //Compute errors matrix and sum-squared error (SSE).
    new_SSE=0;
    for (int i=0;i<S3;i++) {
      for (int j=0;j<Q;j++) {
        new_errors[i][j] = targets[i][j] - new_out3[i][j];
        new_SSE += new_errors[i][j]*new_errors[i][j];
      }
    }
    // MOMENTUM AND ADAPTIVE LEARNING RATE PHASE
    if (new_SSE > SSE*er) {
      lr *= dm;
      MC=0; // kill momentum off
    }
    else {
      if (new_SSE < SSE) {
        lr *= im;
      }
      //Rotate pointers between old and new matrices
      swap=W1; W1=new_W1; new_W1=swap;
      swap=W2; W2=new_W2; new_W2=swap;
      swap=W3; W3=new_W3; new_W3=swap;
      swap=b1; b1=new_b1; new_b1=swap;
      swap=b2; b2=new_b2; new_b2=swap;
      swap=b3; b3=new_b3; new_b3=swap;
      swap=out1; out1=new_out1; new_out1=swap;
      swap=out2; out2=new_out2; new_out2=swap;
      swap=out3; out3=new_out3; new_out3=swap;
      swap=errors; errors=new_errors; new_errors=swap;
      SSE=new_SSE;
    }
    //BACKPROPAGATION PHASE
    d3 = deltalog(out3,errors);
    d2 = deltalog(out2,d3,W3);
    d1 = deltalog(out1,d2,W2);
  
    return SSE;
  }

/**
//@return Number of training epochs performed so far on the network
*/
  public int getepoch() {
    return epoch;
  }

/**
@param n Integer specifying which layer's weight matrix you want. (1 for
input layer, 2 for hidden layer, 3 for output layer)
@return A double[][] matrix containing the current weight matrix of layer n
(has Sn rows, and S(n-1) columns- or R columns if n=1)
*/
  public double[][] getWeightMatrix(int n) {
    double[][] W;
    switch (n) {
      case 1:
      W=W1;
      break;
      case 2:
      W=W2;
      break;
      case 3:
      W=W3;
      break;
      default:
      System.out.println("getWeightMatrix: Invalid parameter");
      return null;
    }
    return W;
  }

/**
@param n Integer specifying which layer's bias vector you want. (1 for
input layer, 2 for hidden layer, 3 for output layer)
@return double[][] containing the current bias vector of layer n
(has Sn rows, one column)
*/
  public double[][] getBiasVector(int n) {
    double[][] b;
    switch (n) {
      case 1:
      b=b1;
      break;
      case 2:
      b=b2;
      break;
      case 3:
      b=b3;
      break;
      default:
      System.out.println("getBiasVector: Invalid parameter");
      return null;
    }
    return b;
  }

//---------------------------------------------------------------------
//-------- END OF PUBLIC API -------- PRIVATE METHODS FOLLOW ----------
//---------------------------------------------------------------------
/*---------------------------------------------------------------------
PRIVATE METHOD: InitWB
PARAMETERS: int S, int R, int n
RETURNS: void
ALTERS CLASS VARIABLES: W1,b1,W2,b2,W3,b3, depending on n
READS CLASS VARIABLES: none
This method is the Nguyen-Widrow random initializer for log-sigmoid neurons.
It establishes a properly sized and randomly initialized weight matrix and
bias vector for neuron layer n (comprised of S neurons) to use in
processing its R input values. This method is called by the constructor.
*/
  private void InitWB(int S,int R,int n) {
    //Method to establish a properly sized and randomly initialized
    //weight matrix and bias vector for the neuron layer n (comprised
    //of S neurons) to use in processing its R input values.

    double[][] w = new double[S][R];
    double[][] b = new double[S][1];
    double acc,acc2;
    double magw;

    magw=2.8*(S^(1/R));
    for (int i=0;i<S;i++) {
      b[i][0]=(2*Math.random())-1;
      acc=0;
      for (int j=0;j<R;j++) {
        w[i][j]=(2*Math.random())-1;
        acc += (w[i][j]*w[i][j]);
      }
    // Normalize the "neuron i" row vector (in w) created in previous loop
      acc = Math.sqrt(1/acc);
      acc2 = 0;
      for (int j=0;j<R;j++) {
        w[i][j] *= (2 * acc);
        acc2 += w[i][j];
      }
      //Normalize "neuron i" bias vector b with respect to its weights in w
      b[i][0] -= (acc2/2);
    }
    //Now w and b are initialized and "presentable".
    switch (n) {
      case 1:
        W1=w;
        b1=b;
      break;
      case 2:
        W2=w;
        b2=b;
      break;
      case 3:
        W3=w;
        b3=b;
      break;
      default:
      System.out.println("InitWB: Invalid network layer argument.");
    }
  }

/*---------------------------------------------------------------------
PRIVATE METHOD: logsig
PARAMETERS: double[][] ip, double[][] b
RETURNS: double [][] output from a log-sigmoid transfer function
ALTERS CLASS VARIABLES: none
READS CLASS VARIABLES: none
logsig adds the bias vector element from b to each element in the
corresponding row of ip (which is a matrix product of weights x inputs).
It then subjects each element of the result to a log-sigmoid transfer
function 1/(1+e^-x) and returns the resulting array.
*/
  private double[][] logsig( double[][] ip, double[][] b ) {
    int[] ip_dims = getSize(ip);
    double[][] out = new double[ip_dims[0]][ip_dims[1]];
    for (int i=0;i<ip_dims[0];i++) {
      for (int j=0;j<ip_dims[1];j++) {
        ip[i][j] += b[i][0];
        out[i][j] = 1/(1+Math.exp(-ip[i][j]));
      }
    }
    return out;
  }

/*---------------------------------------------------------------------
PRIVATE METHOD: deltalog
PARAMETERS: double[][] out, double[][] err
RETURNS: double [][] matrix of derivatives of error for an output layer.
ALTERS CLASS VARIABLES: none
READS CLASS VARIABLES: none
This version of deltalog takes two arguments: out[][], an SnxQ matrix
of outputs from layer n, and err[][], an SnxQ matrix of associated errors
from layer n.  The value returned is an SnxQ matrix of derivatives of
error for the output layer.  This version is used for the output layer only.
The input and hidden layers do not have their associated errors readily
available and they must be calculated indirectly using the chain rule of
derivatives.
*/
  private double[][] deltalog( double[][] out, double[][] err) {
    int[] dims=getSize(out); //should be same as getSize(err)
    double[][] delta= new double[dims[0]][dims[1]];

    for (int i=0;i<dims[0];i++){
      for (int j=0;j<dims[1];j++) {
        delta[i][j]=out[i][j]*(1-out[i][j])*err[i][j];
      }
    }
    return delta;
  }

/*---------------------------------------------------------------------
PRIVATE METHOD: deltalog
PARAMETERS: double[][] out, double[][] d, double[][] w
RETURNS: double [][] matrix of derivatives of error for a layer.
ALTERS CLASS VARIABLES: none
READS CLASS VARIABLES: none
This version of deltalog takes 3 arguments: out[][], an SnxQ matrix
of outputs from layer n, d[][], the S(n+1)xQ matrix of derivatives of
error for the succeeding layer, and w, the weight matrix for that layer.
It calculates the SnxQ matrix of associated errors for layer n by applying
the chain rule of derivatives, using the weight matrix and error derivative
of the previous layer. This version is used for the input and hidden
layers only. The output layer has its associated error available already,
and does not need to have it computed.
*/
  private double[][] deltalog( double[][] out, double[][] d, double[][] w) {
    // out is an SnxQ matrix of outputs from layer n.
    // d is an SnxQ matrix of derivatives of error for layer n+1.
    // w is the associated weight matrix of layer n+1.
    // This method computes err- the SnxQ matrix of associated errors
    // for layer n- using d and w.
    // This method is typically used for the input and hidden layers only.

    int[] dims=getSize(out);
    double[][] delta = new double[dims[0]][dims[1]];
    double[][] wt = transpose(w);
    double[][] err = multiply(wt,d);
    for (int i=0;i<dims[0];i++){
      for (int j=0;j<dims[1];j++) {
        delta[i][j]=out[i][j]*(1-out[i][j])*err[i][j];
      }
    }
    return delta;
  }

/*---------------------------------------------------------------------
PRIVATE METHOD: multiply
PARAMETERS: double[][] A, double[][] B
RETURNS: double [][] AxB, the inner product of the matrix arguments.
ALTERS CLASS VARIABLES: none
READS CLASS VARIABLES: none
This is simply a method to multiply two matrices.  It is used frequently.
If A and B are not compatible for multiplication, this method returns a
null value.
*/
  private double[][] multiply(double[][] A, double[][] B) {
    // Method to multiply two matrices: AxB=C

    int[] dimA = getSize(A);
    int[] dimB = getSize(B);
    int Am = dimA[0];
    int An = dimA[1];
    int Bm = dimB[0];
    int Bn = dimB[1];
    int[] tmp = getSize(B);

    if (An != Bm) {
      // # columns in A must equal # rows in B for AxB to be defined
      System.out.println("multiply error");
      System.out.println("rows in A "+Am);
      System.out.println("cols in A "+An);
      System.out.println("rows in B "+Bm);
      System.out.println("cols in B "+Bn);
      return null;
    }

    double[][] C = new double[Am][Bn];
    for (int i=0;i<Am;i++) {
      for (int j=0;j<Bn;j++) {
        C[i][j]=0;
        for (int k=0;k<An;k++) {
          C[i][j] += A[i][k]*B[k][j];
        }
      }
    }
    return C;
  }

/*---------------------------------------------------------------------
PRIVATE METHOD: transpose
PARAMETERS: double[][] A
RETURNS: double [][] transpose of matrix argument
ALTERS CLASS VARIABLES: none
READS CLASS VARIABLES: none
This method simply returns the transpose of a matrix.  It is called
by the three-argument version of deltalog.
*/
  private double[][] transpose(double[][] A) {
    // returns transpose of a matrix A
    int[] dim_A = getSize(A);
    int m=dim_A[0];
    int n=dim_A[1];
    double[][] At = new double[n][m];
    for (int i=0;i<m;i++) {
      for (int j=0;j<n;j++) {
        At[j][i]=A[i][j];
      }
    }
    return At;
  }
/*---------------------------------------------------------------------
PRIVATE METHOD: getSize
PARAMETERS: double[][] A
RETURNS: int[] containing dimensions of A
ALTERS CLASS VARIABLES: none
READS CLASS VARIABLES: none
Java's array "length" field only returns the number of rows in a
2-D array- there is no easy way to get the number of columns.
This method will return a two-element int array containing the
dimension of its 2-D double[][] argument, i.e. number of rows and
columns contained in a RECTANGULAR double[][] array A. It searches
for the lowest array column index that will generate an
ArrayIndexOutOfBoundsException. This method only scans the length
of the first row. It is not expecting funny arrays that are triangular
or otherwise irregularly shaped but it will still work on them.
*/
  private int[] getSize(double[][] A) {

    double temp;
    boolean gotCols;
    int[] dims=new int[2];
    int stepsize,Cols;

    dims[0]=A.length;  //The # of rows is the easy part!

    stepsize=1024;
    Cols=stepsize;
    gotCols=false;
    while (gotCols==false) {
      try {
        temp=A[0][Cols];
      //Executed normally... Cols is within bounds of array
        Cols += stepsize;
      }
      catch (java.lang.ArrayIndexOutOfBoundsException e) {
        // Uh oh, index "Cols" is out of bounds!
        if (stepsize==1) { gotCols=true; }
        else {
          Cols -= stepsize;
          stepsize /= 2;
        }
      }
    }
    dims[1]=Cols;
    return dims;    
  }

/*---------------------------------------------------------------------
PRIVATE METHOD: diagnostic
PARAMETERS: double[][] testarray, String message
RETURNS: void
ALTERS CLASS VARIABLES: none
READS CLASS VARIABLES: none
This is a debugging method that I decided to leave in here because it
may prove handy to anyone modifying the code. All it does is spit out
the sum and sum of squares of the first column of an input matrix to
the console output, preceded by the String argument. It is useful if
you want to determine how array contents are changing between
subsequent calls.
*/
  private void diagnostic(double[][] testarray, String message) {
    int M = testarray.length;
    double acc = 0;
    double accsq = 0;
    for (int i=0;i<M;i++) {
      acc += testarray[i][0];
      accsq += (testarray[i][0]*testarray[i][0]);
    }
    System.out.println(message);
    System.out.println("acc "+acc+"  accsq "+accsq);
    System.out.println("-------------------------------");
  }
}
