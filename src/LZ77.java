import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class LZ77 {
    private String originalStream;
    private ArrayList<Tag> tags;

    public void compress(String inputFilePath, String outputFilePath) {
        Tag t = new Tag();
        tags = new ArrayList<Tag>();
        originalStream = "";
        String buffer = "";
        String search = "";
        try{
            BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
            while(reader.ready()){
                originalStream += reader.readLine();
            }
            reader.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        for (int i = 0; i < originalStream.length(); i++) {
            while (search.length() < 15) {
                if(i >= originalStream.length()){
                    break;
                }
                t = new Tag();
                search += originalStream.charAt(i);
                int position = buffer.indexOf(search);
                if (position == -1) {
                    t.setPosition(buffer.length() - buffer.indexOf(search.substring(0, search.length() - 1)));
                    t.setLength(search.length() - 1);
                    if (t.getLength() == 0) {
                        t.setPosition(0);
                    }
                    t.setNextChar(originalStream.charAt(i));
                    tags.add(t);
                    if (buffer.length() < 15 && buffer.length() + search.length() <= 15) {
                        buffer += search;
                    } else {
                        buffer = buffer.substring(search.length() - (15 - buffer.length()));
                        buffer += search;
                    }
                    break;
                } else if (position != -1 && buffer.length() == search.length() && buffer.length() == 15) {
                    t.setPosition(15);
                    t.setLength(15);
                    t.setNextChar(originalStream.charAt(i + 1));
                    tags.add(t);
                    break;
                }
                i++;
                if(i >= originalStream.length()){
                    t.setPosition(buffer.length() - buffer.indexOf(search.substring(0, search.length())));
                    t.setLength(search.length());
                    t.setNextChar('\0');
                    tags.add(t);
                    break;
                }
            }
            search = "";
        }
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath));
            for(Tag tag : tags){
                writer.write(tag.getPosition() + " " + tag.getLength() + " " + tag.getNextChar() + "\n");
            }
            writer.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        
    }

    public void decompress(String inputFilePath, String outputFilePath) {
        originalStream = "";
        tags = new ArrayList<Tag>();
        try{
            BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
            while(reader.ready()){
                String[] current = reader.readLine().split(" ");
                Tag t = new Tag();
                t.setPosition(Integer.parseInt(current[0]));
                t.setLength(Integer.parseInt(current[1]));
                t.setNextChar(current[2].charAt(0));
                tags.add(t);
            }
            reader.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        for (Tag tag : tags) {
            int P = tag.getPosition();
            int L = tag.getLength();
            char N = tag.getNextChar();
            if (N == '\0') {
                originalStream += originalStream.substring(originalStream.length() - P , originalStream.length() - P + L );
            } else {
                originalStream += originalStream.substring(originalStream.length() - P, originalStream.length() - P + L) + N;
            }
        }
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath));
            writer.write(originalStream);
            writer.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    // public static void main(String[] args) {
    //     LZ77 lz77 = new LZ77();
    //     lz77.compress("src/input.txt", "src/compressed.txt");
    //     lz77.decompress("src/compressed.txt", "src/decompressed.txt");
    // }
}

