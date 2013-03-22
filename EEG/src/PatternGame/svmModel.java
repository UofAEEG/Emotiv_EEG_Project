package PatternGame;
/*
import libsvm.*;

public class svmModel extends svm {
	
	public svm_problem problem;
	public svm_parameter parameters;
	public svm_model model;
*/	

/*  This is is copied from the official SVM documentation
 * 	svm_type can be one of C_SVC, NU_SVC, ONE_CLASS, EPSILON_SVR, NU_SVR.

    C_SVC:		C-SVM classification
    NU_SVC:		nu-SVM classification
    ONE_CLASS:		one-class-SVM
    EPSILON_SVR:	epsilon-SVM regression
    NU_SVR:		nu-SVM regression

    kernel_type can be one of LINEAR, POLY, RBF, SIGMOID.

    LINEAR:	u'*v
    POLY:	(gamma*u'*v + coef0)^degree
    RBF:	exp(-gamma*|u-v|^2)
    SIGMOID:	tanh(gamma*u'*v + coef0)
    PRECOMPUTED: kernel values in training_set_file

    cache_size is the size of the kernel cache, specified in megabytes.
    C is the cost of constraints violation. 
    eps is the stopping criterion. (we usually use 0.00001 in nu-SVC,
    0.001 in others). nu is the parameter in nu-SVM, nu-SVR, and
    one-class-SVM. p is the epsilon in epsilon-insensitive loss function
    of epsilon-SVM regression. shrinking = 1 means shrinking is conducted;
    = 0 otherwise. probability = 1 means model with probability
    information is obtained; = 0 otherwise.

    nr_weight, weight_label, and weight are used to change the penalty
    for some classes (If the weight for a class is not changed, it is
    set to 1). This is useful for training classifier using unbalanced
    input data or with asymmetric misclassification cost.

    nr_weight is the number of elements in the array weight_label and
    weight. Each weight[i] corresponds to weight_label[i], meaning that
    the penalty of class weight_label[i] is scaled by a factor of weight[i].
    
    If you do not want to change penalty for any of the classes,
    just set nr_weight to 0.
    */

/*
	public svmModel() {
		super();
		this.problem = new svm_problem();
		problem.l = 1;
		problem.y = new double[0];
		problem.x = new svm_node[0][0];
		
		this.parameters = new svm_parameter();
		parameters.svm_type = svm_parameter.C_SVC;
		parameters.kernel_type = svm_parameter.LINEAR;
		parameters.degree = 0;
		parameters.gamma = 0;
		parameters.coef0 = 0;
		
		parameters.cache_size = 500f;
		parameters.eps = 0.001f;
		parameters.C = 0;
		parameters.nr_weight = 0;
		parameters.shrinking = 0;
		parameters.probability = 0;
		
		System.err.println(svm.svm_check_parameter(problem, parameters));
		
		this.convertMatrix();
		
		this.model = svm.svm_train(problem, parameters);
	}
	
	public double[] predictPattern(svm_node[] testingData)
	{
		double[] decisionValues = null;
		svm_predict_values(model, testingData, decisionValues);
		
		return decisionValues;
	}
*/
/*	The format of training and testing data file is:

		<label> <index1>:<value1> <index2>:<value2> ...
		.
		.
		.
		
	Gonna use this private method to convert provide matrices to training data*/

/*	
	private svm_node[]  convertMatrix(){
		svm_node[] data = null;
		return data;
	}
	

}
*/