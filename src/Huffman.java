import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Huffman {
    private HashMap<Character, String> codes;
    private HashMap<Character, Integer> frequencies;
    void countFreq(String originalStream){
        frequencies = new HashMap<>();
        for (int i = 0; i < originalStream.length(); i++) {
            char current = originalStream.charAt(i);
            if (frequencies.containsKey(current)) {
                frequencies.put(current, frequencies.get(current) + 1);
            } else {
                frequencies.put(current, 1);
            }
        }
    }

    void buildTree(){
        ArrayList<Node> nodes = new ArrayList<>();
        for (char c : frequencies.keySet()) {
            nodes.add(new Node(c, frequencies.get(c)));
        }
        nodes.sort((n1, n2) -> n1.getFrequency() - n2.getFrequency());
        while (nodes.size() > 1) {
            Node left = nodes.remove(0);
            Node right = nodes.remove(0);
            Node parent = new Node(left, right);
            nodes.add(parent);
            nodes.sort((n1, n2) -> n1.getFrequency() - n2.getFrequency());
        }
        codes = new HashMap<>();
        nodes.get(0).traverse(codes, "");
    }


    void compress(String inputFilePath, String outputFilePath) {
        try{
            BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath));

            String originalStream = "";
            while (reader.ready()) {
                originalStream += reader.readLine();
            }
            countFreq(originalStream);
            buildTree();
            for (int i = 0; i < originalStream.length(); i++) {
                writer.write(codes.get(originalStream.charAt(i)));
            }
            writer.write("\n");
            for (char c : codes.keySet()) {
                writer.write(c + " " + codes.get(c) + "\n");
            }
            reader.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private String ReadFile(String inputFilePath){
        String compressedStream = "";
        codes = new HashMap<>();
        try{
            BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
            boolean firstLine = true;
            while (reader.ready()) {
                String line = reader.readLine();
                if(firstLine){
                    firstLine = false;
                    compressedStream = line;
                    continue;
                }
                char c = line.charAt(0);
                String code = line.substring(2);
                codes.put(c, code);
            }
            reader.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return compressedStream;
        
    }
    
    void decompress(String inputFilePath, String outputFilePath) {
        String compressedStream = ReadFile(inputFilePath);
        String decompressedStream = "";
        String currentCode = "";

        for (int i = 0; i < compressedStream.length(); i++) {
            currentCode += compressedStream.charAt(i);
            for (char c : codes.keySet()) {
                if (codes.get(c).equals(currentCode)) {
                    decompressedStream += c;
                    currentCode = "";
                    break;
                }
            }
        }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath));
            writer.write(decompressedStream);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Node{
    private char character;
    private int frequency;
    private Node left;
    private Node right;
    public Node(char character, int frequency) {
        this.character = character;
        this.frequency = frequency;
    }
    public Node(Node left, Node right) {
        this.left = left;
        this.right = right;
        this.frequency = left.getFrequency() + right.getFrequency();
    }
    public int getFrequency() {
        return frequency;
    }
    public void traverse(HashMap<Character, String> codes, String currentCode) {
        if (this.character != '\0') {
            codes.put(this.character, currentCode);
            return;
        }
        this.left.traverse(codes, currentCode + "0");
        this.right.traverse(codes, currentCode + "1");
    }
}