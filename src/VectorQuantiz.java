import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import javax.imageio.ImageIO;
import java.io.FileWriter;



public class VectorQuantiz {
    ArrayList<int[][]> vectors = new ArrayList<>();
    ArrayList<double[][]> codebook = new ArrayList<>();
    HashMap<Integer, Integer> codebookMap = new HashMap<>();
    HashMap<Integer, String> binaryCode = new HashMap<>();
    int[][] img;

    

    void getImg(String path) throws Exception {
        BufferedImage imgRead = ImageIO.read(new File(path));
        img = new int[imgRead.getWidth()][imgRead.getHeight()];
        for (int i = 0; i < imgRead.getWidth(); i++) {
            for (int j = 0; j < imgRead.getHeight(); j++) {
                img[i][j] = imgRead.getRGB(i, j);
            }
        }
    }

    void generateCodebookA(int vectorSize) {
        double sum = 0;
        int total = (img.length / vectorSize) * (img.length / vectorSize);
        double[][] vector = new double[vectorSize][vectorSize];
    
        for(int j = 0; j < vectorSize; j++){
            for(int k = 0; k < vectorSize; k++){
                for(int i = 0; i < vectors.size(); i++){
                    sum += vectors.get(i)[j][k];
                }
                vector[j][k] = sum / total;
                sum = 0;
            }
        }
        codebook.add(vector);
        
    }
    void generateCodebookM(int vectorSize) {
        double sum = 0;
        int total = (img.length / vectorSize) * (img.length / vectorSize);
        double[][] vector = new double[vectorSize][vectorSize];
        ArrayList<int[][]> nearestVectors = new ArrayList<>();
        int size = codebook.size();
        for (int i = 0; i <= size - i; i++) {
            for (int m : codebookMap.keySet()) {
                if(codebookMap.get(m) == i){
                    nearestVectors.add(vectors.get(m));
                }
            }
            for(int j = 0; j < vectorSize; j++){
                for(int k = 0; k < vectorSize; k++){
                    for(int l = 0; l < nearestVectors.size(); l++){
                        sum += nearestVectors.get(l)[j][k];
                    }
                    vector[j][k] = sum / total;
                    sum = 0;
                }
            }
            codebook.remove(0);
            codebook.add(vector);
            nearestVectors.clear();
        }
    }

    void codebookSplit(int vectorSize){
        double[][] vector1 = new double[vectorSize][vectorSize];
        double[][] vector2 = new double[vectorSize][vectorSize];
        int size = codebook.size();
        for (int i = 0; i <= size - i; i++) {
            for (int j = 0; j < vectorSize; j++) {
                for (int k = 0; k < vectorSize; k++) {
                    vector1[j][k] =  Math.floor(codebook.get(0)[j][k]);
                    vector2[j][k] =  Math.ceil(codebook.get(0)[j][k]);
                }
            }
            codebook.remove(0);
            codebook.add(vector1);
            codebook.add(vector2);
        }
    }

    void nearestVector(int vectorSize){
        ArrayList<Double> distance = new ArrayList<>();
        double sum = 0;
        for (int i = 0; i < codebook.size(); i++) {
            for (int j = 0; j < vectorSize; j++) {
                for (int k = 0; k < vectorSize; k++) {
                    sum += codebook.get(i)[j][k];
                }
            }
            distance.add(sum);
            sum = 0;
        }

        for (int i = 0; i < vectors.size(); i++) {
            
            for (int j = 0; j < vectorSize; j++) {
                for (int k = 0; k < vectorSize; k++) {
                    sum += vectors.get(i)[j][k];
                }
            }
            double min = Math.abs(sum - distance.get(0));
            int index = 0;
            for (int j = 1; j < distance.size(); j++) {
                if(Math.abs(sum - distance.get(j)) < min){
                    min = Math.abs(sum - distance.get(j));
                    index = j;
                }
            }
            sum = 0;
            codebookMap.put(i, index);
        }

    }


    void writeToFile(String path, String binary) {
        try {
            FileOutputStream writer = new FileOutputStream(path, true);
            byte[] bytes = new byte[binary.length() / 8];
            for (int i = 0; i < binary.length(); i += 8) {
                String byteString = binary.substring(i, i + 8);
                byte b = (byte) Integer.parseInt(byteString, 2);
                bytes[i / 8] = b;
            }
            for (int i = 0; i < bytes.length; i++) {
                writer.write(bytes[i]);
            }
            
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String padBinaryString(String binaryString) {
        
        int remainder = binaryString.length() % 8;
        if (remainder != 0) {
            int padLength = 8 - remainder;
            StringBuilder paddedBinaryString = new StringBuilder(binaryString);
            for (int i = 0; i < padLength; i++) {
                paddedBinaryString.insert(0, '0');
            }
            return paddedBinaryString.toString();
        }
        return binaryString;
    }
    void getimg(String path) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(path));
        img = new int[6][6];
        int i = 0;
        while (reader.ready()) {
            String line = reader.readLine();
            String[] values = line.split(" ");
            for (int j = 0; j < 6; j++) {
                img[i][j] = Integer.parseInt(values[j]);
            }
            i++;
        }
    }
    public void compress(String inputFilePath, String outputFilePath, int Codebook, int vectorSize) {
        try {
            //getImg(inputFilePath);
            getimg(inputFilePath);
            for (int i = 0; i < 6 ; i+= vectorSize) {
                for (int j = 0; j < 6 ; j+= vectorSize) {
                    int[][] vector = new int[vectorSize][vectorSize];
                    for (int k = 0; k < vectorSize; k++) {
                        for (int l = 0; l < vectorSize; l++) {
                            vector[k][l] = img[i + k][j + l];
                        }
                    }
                    vectors.add(vector);
                }
            }
            boolean flag = true;
            for (int i = 0; i <= Math.log(Codebook); i++) {
                if(flag){
                    generateCodebookA(vectorSize);
                    flag = false;
                }
                else{
                    generateCodebookM(vectorSize);
                }
                codebookSplit(vectorSize);
                nearestVector(vectorSize);
            }
            
            for(int i = 0; i < codebook.size(); i++){
                binaryCode.put(i, Integer.toBinaryString(i));
            }

            String binary = "";
            
            for (int i = 0; i < vectors.size(); i++) {
                binary += binaryCode.get(codebookMap.get(i));
                if((i + 1) % (img.length / vectorSize) == 0){
                    String paddedBinary = padBinaryString(binary);
                    BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath, true));
                    writer.write( paddedBinary.length() - binary.length() + "");
                    writer.close();
                    writeToFile(outputFilePath, paddedBinary);
                    writer = new BufferedWriter(new FileWriter(outputFilePath, true));
                    writer.write("\n");
                    writer.close();
                    binary = "";
                }
            }
            

        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    void decompress(String inputFilePath, String outputFilePath) {
        
    }
    
    
    
}
