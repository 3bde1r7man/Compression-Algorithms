/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Abdelrhman Mostafa
 * @author Ahmed Hanfy
 * @Date 3/11/2023
 * 
 */


public class LZW {
    private Dictionary dictionary;
    // compress method takes the input file path and the output file path
    public void compress(String inputFilePath, String outputFilePath) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath));
            this.dictionary = new Dictionary();
            for (int i = 65; i < 128; i++) {
                this.dictionary.add("" + (char) i);
            }
            // read the input file and store it in a string
            String originalStream = "";
            while (reader.ready()) {
                originalStream += reader.readLine();
            }
            // compress the string 
            for (int i = 0; i < originalStream.length(); i++) {
                String current = "" + originalStream.charAt(i);
                // check if the current string is in the dictionary
                while (this.dictionary.contains(current) && i < originalStream.length() - 1) {
                    i++;
                    current += originalStream.charAt(i);
                }
                // if the current string is not in the dictionary, add it to the dictionary
                if (this.dictionary.contains(current)) {
                    writer.write(this.dictionary.get(current) + " ");
                } else { 
                    this.dictionary.add(current);
                    writer.write(this.dictionary.get(current.substring(0, current.length() - 1)) + " ");
                    // to avoid skipping the last character in the string
                    i--;
                }
            }

            reader.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // decompress method takes the input file path and the output file path
    public void decompress(String inputFilePath, String outputFilePath) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath));
            this.dictionary = new Dictionary();
            for (int i = 65; i < 128; i++) {
                this.dictionary.add("" + (char) i);
            }
            // read the input file and store it in a string
            while (reader.ready()) {
                String[] current = reader.readLine().split(" ");
                int previousIndex = Integer.parseInt(current[0]);
                writer.write(this.dictionary.get(previousIndex));
                // decompress the string 
                for (int i = 1; i < current.length; i++) {
                    int currentIndex = Integer.parseInt(current[i]);
                    String next = this.dictionary.get(currentIndex);
                    // if the current string is not in the dictionary, add it to the dictionary
                    if (next == null) {
                        next = this.dictionary.get(previousIndex) + this.dictionary.get(previousIndex).charAt(0);
                    }
                    writer.write(next);
                    this.dictionary.add(this.dictionary.get(previousIndex) + next.charAt(0));
                    previousIndex = currentIndex;
                }
            }
            reader.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // public static void main(String[] args) {
    //     LZW lzw = new LZW();
    //     lzw.compress("src/main/java/lzw/lzw/input.txt", "src/main/java/lzw/lzw/compressed.txt");
    //     lzw.decompress("src/main/java/lzw/lzw/compressed.txt", "src/main/java/lzw/lzw/decompressed.txt");
    // }   
}
