class Tag {
    private int position;
    private int length;
    private char nextChar;

    public void setPosition(int p) {
        position = p;
    }

    public void setLength(int l) {
        length = l;
    }

    public void setNextChar(char c) {
        nextChar = c;
    }

    public int getPosition() {
        return position;
    }

    public int getLength() {
        return length;
    }

    public char getNextChar() {
        return nextChar;
    }
}