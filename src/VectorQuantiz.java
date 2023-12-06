import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import javax.imageio.ImageIO;
import java.io.FileWriter;
import java.io.IOException;




public class VectorQuantiz {
    ArrayList<int[][]> vectors = new ArrayList<>();
    ArrayList<int[][]> vectorsD;
    ArrayList<double[][]> codebook = new ArrayList<>();
    HashMap<Integer, Integer> codebookMap = new HashMap<>();
    HashMap<Integer, String> binaryCode = new HashMap<>();
    int[][] img;


    int[][] readImage(String filePath) {
		File file = new File(filePath);
		BufferedImage image = null;
		try {
			image = ImageIO.read(file);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		int width = image.getWidth();
		int height = image.getHeight();
		int[][] pixels = new int[height][width];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int p = image.getRGB(x, y);
				int a = (p >> 24) & 0xff; // & 0xff to get the last 8 bits that represent the value of the pixel
				int r = (p >> 16) & 0xff; // >> 16 to get the 8 bits that represent the red value of the pixel
				int g = (p >> 8) & 0xff; // >> 8 to get the 8 bits that represent the green value of the pixel
				int b = p & 0xff; // to get the 8 bits that represent the blue value of the pixel

				pixels[y][x] = r; // since the image is gray scale, we can use any of the r, g, b values to get the
									// pixel value

				p = (a << 24) | (r << 16) | (g << 8) | b; // to get the pixel value in the form of an integer
				image.setRGB(x, y, p);
			}

		}
		return pixels;
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
        
        
        ArrayList<int[][]> nearestVectors = new ArrayList<>();
        int size = codebook.size();
        for (int i = 0; i <= size - i; i++) {
            for (int m : codebookMap.keySet()) {
                if(codebookMap.get(m) == i){
                    nearestVectors.add(vectors.get(m));
                }
            }
            double[][] vector = new double[vectorSize][vectorSize];
            for(int j = 0; j < vectorSize; j++){
                for(int k = 0; k < vectorSize; k++){
                    for(int l = 0; l < nearestVectors.size(); l++){
                        sum += nearestVectors.get(l)[j][k];
                    }
                    vector[j][k] = sum / nearestVectors.size();
                    sum = 0;
                }
            }
            codebook.remove(0);
            codebook.add(vector);
            nearestVectors.clear();
        }
    }

    void codebookSplit(int vectorSize){
        
        int size = codebook.size();
        for (int i = 0; i < size; i++) {
            double[][] vector1 = new double[vectorSize][vectorSize];
            double[][] vector2 = new double[vectorSize][vectorSize];
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

        for (int i = 0; i < vectors.size(); i++) {
            for (int j = 0; j < codebook.size(); j++) {
                double sum = 0;
                for (int k = 0; k < vectorSize; k++) {
                    for (int l = 0; l < vectorSize; l++) {
                        sum += Math.abs(vectors.get(i)[k][l] - codebook.get(j)[k][l]);
                    }
                }
                distance.add(sum);
                sum = 0;
            }
            double min = distance.get(0);
            int index = 0;
            for (int j = 1; j < distance.size(); j++) {
                if(distance.get(j) < min){
                    min = distance.get(j);
                    index = j;
                }
            }
            distance.clear();
            codebookMap.put(i, index);
        }
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
        reader.close();
    }

    String fixCodeBook(int codebook, String code){
        while (code.length() < (Math.log(codebook)/ Math.log(2))) {
            code = "0" + code;
        }
        return code;
    }

    void WriteOverHead(String outputFilePath, int vectorSize, int codebookSize){
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath, true));
            writer.write(codebookSize + ""); 
            writer.write(' ');
            writer.write(vectorSize + "");
            writer.write('\n');
            for (int i = 0; i < binaryCode.size(); i++) {
                String code = binaryCode.get(i);
                writer.write(code + " ");
                for (int j = 0; j < vectorSize; j++) {
                    for (int k = 0; k < vectorSize; k++) {
                        writer.write((int)codebook.get(i)[j][k] + " ");
                    }
                }
            }
            writer.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public void compress(String inputFilePath, String outputFilePath, int Codebook, int vectorSize) {
        try {
            
            img = readImage(inputFilePath);
            //getimg(inputFilePath);
            for (int i = 0; i < img.length; i+= vectorSize) {
                for (int j = 0; j < img.length; j+= vectorSize) {
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
            for (int i = 0; i < (Math.log(Codebook) / Math.log(2)); i++) {
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
                String code = Integer.toBinaryString(i);
                code = fixCodeBook(codebook.size(), code);
                binaryCode.put(i, code);
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath));
            writer.write((img.length / vectorSize) + "\n");
            for (int i = 0; i < vectors.size(); i++) {
                writer.write(binaryCode.get(codebookMap.get(i)));
                if((i+ 1) % (vectors.size() / (img.length / vectorSize)) == 0){
                    writer.write('\n');
                }
            }
            writer.close();
            WriteOverHead(outputFilePath, vectorSize, Codebook);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    ArrayList<String> readBinaryFile(String inputFilePath) throws Exception{
        ArrayList<String> commpressedStream = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
        int lines = Integer.parseInt(reader.readLine());
        for (int i = 0; i < lines; i++) {
            String line = reader.readLine();
            commpressedStream.add(line);
        }
        String line = reader.readLine();
        int codebookSize = Integer.parseInt(line.split(" ")[0]);
        int vectorSize = Integer.parseInt(line.split(" ")[1]);
        line = reader.readLine();
        String[] overHead = line.split(" "); 
        codebook = new ArrayList<>();
        binaryCode = new HashMap<>();
        for (int i = 0; i < codebookSize; i++) {
            double[][] vector = new double[vectorSize][vectorSize];
            String code = overHead[i * ((vectorSize * vectorSize) + 1)];
            binaryCode.put(i, code);
            for (int j = 0; j < vectorSize; j++) {
                for (int k = 0; k < vectorSize; k++) {
                    vector[j][k] = Integer.parseInt(overHead[(i * vectorSize * vectorSize) + (j * vectorSize) + k + 1]);
                }
            }
            codebook.add(vector);
        }
        reader.close();
        return commpressedStream;

    }
    void decompress(String inputFilePath, String outputFilePath) {
        try{
            HashMap<String, Integer> binMap = new HashMap<>();
            vectorsD = new ArrayList<>();

            ArrayList<String> commpressedStream = readBinaryFile(inputFilePath);
            for (int i = 0; i < binaryCode.size(); i++) {
                binMap.put(binaryCode.get(i), i);
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath));
            vectorsD = new ArrayList<>();
            for (int i = 0; i < commpressedStream.size(); i++) {
                String line = commpressedStream.get(i);
                int codebookSize = binaryCode.get(0).length();
                for (int j = 0; j < line.length(); j += codebookSize) {
                    String code = line.substring(j, j + codebookSize);
                    int[][] vector = new int[codebook.get(0).length][codebook.get(0).length];
                    int index = binMap.get(code);
                    for (int k = 0; k < codebook.get(index).length; k++) {
                        for (int l = 0; l < codebook.get(index).length; l++) {
                            vector[k][l] = (int)codebook.get(index)[k][l];
                        }
                    }
                    vectorsD.add(vector);
                }
            }
            int vectorSize = codebook.get(0).length;
            int x = vectorsD.size();
            while (x > 0) {
                for (int i = (vectorsD.size() - x); i < (vectorsD.size() / commpressedStream.size()) + (vectorsD.size() - x); i++) {
                    for (int j = 0; j < vectorSize/2; j++) {
                        for (int k = 0; k < vectorSize; k++) {
                            System.out.print((int)vectorsD.get(i)[j][k] + " ");
                            writer.write((int)vectorsD.get(i)[j][k] + " ");
                        }
                    }
                }
                System.out.println();
                writer.write("\n");
                for (int i = (vectorsD.size() - x); i < (vectorsD.size() / commpressedStream.size()) + (vectorsD.size() - x); i++) {
                    for (int j = vectorSize/2; j < vectorSize; j++) {
                        for (int k = 0; k < vectorSize; k++) {
                            System.out.print((int)vectorsD.get(i)[j][k] + " ");
                            writer.write((int)vectorsD.get(i)[j][k] + " ");
                        }
                    }
                }
                System.out.println();
                writer.write("\n");
                x -= (vectorsD.size() / commpressedStream.size());
            }
            writer.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
