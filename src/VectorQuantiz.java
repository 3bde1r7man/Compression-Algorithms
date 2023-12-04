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
    ArrayList<double[][]> vectorsD;
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
        for (int i = 0; i <= size - i; i++) {
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
            FileOutputStream writer = new FileOutputStream(outputFilePath, true);
            writer.write(codebookSize); 
            writer.write(' ');
            writer.write(vectorSize);
            writer.write('\n');
            for (int i = 0; i < binaryCode.size(); i++) {
                String code = binaryCode.get(i);

                for (int j = 0; j < code.length(); j++) {
                    writer.write((char)code.charAt(j));
                }
                for (int j = 0; j < vectorSize; j++) {
                    for (int k = 0; k < vectorSize; k++) {
                        writer.write((int)codebook.get(i)[j][k]);
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
                String code = Integer.toBinaryString(i);
                code = fixCodeBook(codebook.size(), code);
                binaryCode.put(i, code);
            }

            String binary = "";
            FileOutputStream wr = new FileOutputStream(outputFilePath);
            wr.write((img.length / vectorSize));
            wr.write('\n');
            wr.close();
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
            WriteOverHead(outputFilePath, vectorSize, Codebook);

        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    String repairBinary(String binary){
        while (binary.length() < 8) {
            binary = "0" + binary;
        }
        while (binary.length() > 8) {
            binary = binary.substring(binary.length() - 8);
        }
        return binary;
    }
    ArrayList<String> readBinaryFile(String inputFilePath) throws Exception{
        ArrayList<String> commpressedStream = new ArrayList<>();
        FileInputStream reader = new FileInputStream(inputFilePath);
        int size = reader.read();
        for (int i = 0; i < size; i++) {
            commpressedStream.add("");
        }
        reader.read();
        int addedzeros = reader.read() - '0';
        for (int i = 0; i < size; i++) {
            while (reader.available() > 0) {
                int current = reader.read();
                if(current == '\n'){
                    if(i == size - 1){
                        break;
                    }
                    addedzeros = reader.read() - '0';
                    break;
                }
                byte b = (byte) current;
                String code = Integer.toBinaryString(b);
                
                commpressedStream.set(i, commpressedStream.get(i) + repairBinary(code));
            }
            commpressedStream.set(i, commpressedStream.get(i).substring(addedzeros));
        }
        int codebookSize = reader.read();
        reader.read();
        int vectorSize = reader.read();
        reader.read();
        codebook = new ArrayList<>();
        binaryCode = new HashMap<>();
        for (int i = 0; i < codebookSize; i++) {
            String code = "";
            char c = '\0';
            for (int j = 0; j < Math.log(codebookSize); j++) {
                int current = reader.read();
                c = (char) current;
                code += c;
            }
            binaryCode.put(i, code);
            double[][] vector = new double[vectorSize][vectorSize];
            for (int j = 0; j < vectorSize; j++) {
                for (int k = 0; k < vectorSize; k++) {
                    vector[j][k] = reader.read();
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
            for (int i = 0; i < commpressedStream.size(); i++) {
                String binary = commpressedStream.get(i);
                for (int j = 0; j < binary.length(); j += (int)(Math.log(codebook.size()) / Math.log(2))) {
                    String code = binary.substring(j, j + (int)(Math.log(codebook.size()) / Math.log(2)));
                    vectorsD.add(codebook.get(binMap.get(code)));
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
                x-= (vectorsD.size() / commpressedStream.size());
            }
            writer.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
