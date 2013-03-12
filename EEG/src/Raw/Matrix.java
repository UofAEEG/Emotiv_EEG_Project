package Raw;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

//I assume Gabor wants us to push this matrix to SVM every time its full
// double [sample] [sensor]
//{[a,b,c,...],[a',b',c',...],[a",b",c",..]}
public class Matrix {
	


final int MATRIX_SIZE = 128;
final int NUM_SENSORS = 14;
double[][] matrix = null;
int matrixNo = 0;

public Matrix() {
    super();
    matrix = new double[MATRIX_SIZE][NUM_SENSORS];
}


/*Write the matrix's data to a text file*/
public void toFile() {
    
    BufferedWriter matrixout = null;
    
    try {
        matrixout = new BufferedWriter(new FileWriter("data/Matrix_" + matrixNo + "_" + RawData.fileName));
    
    
    for (int i=0; i<MATRIX_SIZE; i++) {
        for (int j=0; j<14; j++){
            matrixout.write(Double.toString(matrix[i][j])+" ");
        }
        matrixout.write("\n");
    }
    matrixout.write("\n\n\n\n");
        
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        matrixNo++;
    }
    
}
