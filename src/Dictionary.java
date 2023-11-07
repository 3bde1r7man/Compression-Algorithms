import java.util.HashMap;


// Dictionary class is used to store the dictionary of the LZW algorithm
public class Dictionary {
    private HashMap<String, Integer> dictionary;
    private int nextIndex;
    
    public Dictionary() {
        this.dictionary = new HashMap<String, Integer>();
        this.nextIndex = 65;
    }
    
    public void add(String key) {
        this.dictionary.put(key, this.nextIndex);
        this.nextIndex++;
    }
    
    public boolean contains(String key) {
        return this.dictionary.containsKey(key);
    }
    
    public int get(String key) {
        return this.dictionary.get(key);
    }
    
    public String get(int index) {
        for (String key : this.dictionary.keySet()) {
            if (this.dictionary.get(key) == index) {
                return key;
            }
        }
        return null;
    }

    public int size() {
        return this.dictionary.size();
    }

}
