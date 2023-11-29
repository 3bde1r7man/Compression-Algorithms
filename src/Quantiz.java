import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class Quantiz {
    void compress(String inputFilePath, String outputFilePath) {
        String originalStream = "";
        try{
            BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
            while (reader.ready()) {
                originalStream += reader.readLine();
            }
            reader.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    void decompress(String inputFilePath, String outputFilePath) {
        String compressedStream = "";
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath));
            writer.write(compressedStream);
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
