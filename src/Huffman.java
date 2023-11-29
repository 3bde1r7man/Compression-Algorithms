import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
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

    private void writeBinaryFile(String binaryString, String fileName) {
        byte[] bytes = new byte[binaryString.length() / 8];
        for (int i = 0; i < binaryString.length(); i += 8) {
            String byteString = binaryString.substring(i, i + 8);
            byte b = (byte) Integer.parseInt(byteString, 2);
            bytes[i / 8] = b;
        }
        try  {
            FileOutputStream fos = new FileOutputStream(fileName);
            for (int i = 0; i < bytes.length; i++) {
                fos.write(bytes[i]);
            }
            fos.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    void compress(String inputFilePath, String outputFilePath) {
        try{
            BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath, true));

            String originalStream = "";
            while (reader.ready()) {
                originalStream += reader.readLine();
            }
            countFreq(originalStream);
            buildTree();
            String compressedStream = "";
            for (int i = 0; i < originalStream.length(); i++) {
                compressedStream += codes.get(originalStream.charAt(i));
            }
            String paddedCompressedStream = padBinaryString(compressedStream);
            writeBinaryFile(paddedCompressedStream, outputFilePath);
            writer.write("\n");
            writer.write(paddedCompressedStream.length() - compressedStream.length() + "\n");
            for (char c : codes.keySet()) {
                writer.write(c + "" + codes.get(c) + "\n");
            }
            reader.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private String readBinaryFile(String fileName) {
        String commpressedStream = "";
        codes = new HashMap<>();
        try {
            FileInputStream reader = new FileInputStream(fileName);
            
            int addedzeros = 0;
            while(reader.available() > 0){
                int current = reader.read();
                if(current == '\n'){
                    break;
                }
                byte b = (byte) current;
                String code = Integer.toBinaryString(b);
                commpressedStream += repairBinary(code);
            }
            addedzeros = reader.read() - '0';
            reader.read();
            String code = "";
            char c = '\0';
            while(reader.available() > 0){
                int current = reader.read();
                c = (char) current;
                current = reader.read();
                while (current != '\n') {
                    code += (char)current;
                    current = reader.read();
                }
                codes.put(c, code);
                code = "";
                c = '\0';
            }
            commpressedStream = commpressedStream.substring(addedzeros);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return commpressedStream;
    }

    private String repairBinary(String c){
        while (c.length() < 8) {
            c = "0" + c;
        }
        if (c.length() > 8) {
            c = c.substring(c.length() - 8);
        }
        return c;
    }
    
    void decompress(String inputFilePath, String outputFilePath) {
        String compressedStream = readBinaryFile(inputFilePath);
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
        this.left.traverse(codes, currentCode + "1");
        this.right.traverse(codes, currentCode + "0");
    }
}