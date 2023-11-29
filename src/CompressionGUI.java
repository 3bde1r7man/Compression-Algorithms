

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class CompressionGUI {
    private JFrame frame;
    private JTextField inputFilePathField;
    private JComboBox<String> algorithmComboBox;
    private JRadioButton compressRadioButton;
    private JRadioButton decompressRadioButton;
    private JButton browseButton;
    private JButton executeButton;
    private JLabel statusLabel;

    public CompressionGUI() {
        frame = new JFrame("Compression/Decompression");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 450);
        frame.setLayout(new GridLayout(6, 2));

        inputFilePathField = new JTextField();
        algorithmComboBox = new JComboBox<>(new String[]{"LZW", "LZ77", "Huffman", "Vector Quantization"});
        compressRadioButton = new JRadioButton("Compress");
        decompressRadioButton = new JRadioButton("Decompress");
        ButtonGroup group = new ButtonGroup();
        group.add(compressRadioButton);
        group.add(decompressRadioButton);
        browseButton = new JButton("Browse");
        executeButton = new JButton("Execute");
        statusLabel = new JLabel();

        frame.add(new JLabel("Input File:"));
        frame.add(inputFilePathField);
        frame.add(new JLabel("Algorithm:"));
        frame.add(algorithmComboBox);
        frame.add(compressRadioButton);
        frame.add(decompressRadioButton);
        frame.add(browseButton);
        frame.add(executeButton);
        frame.add(statusLabel);

        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser(new File("."));
                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    inputFilePathField.setText(selectedFile.getAbsolutePath());
                }
            }
        });

        executeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String inputFilePath = inputFilePathField.getText();
                String algorithm = algorithmComboBox.getSelectedItem().toString();
                boolean isCompress = compressRadioButton.isSelected();
                if(algorithm.equals("LZ77")){
                    LZ77 lz77 = new LZ77();
                    if (isCompress) {
                        lz77.compress(inputFilePath, "src/compressed.txt");
                        statusLabel.setText("Compression completed successfully.");
                    } else {
                        lz77.decompress(inputFilePath, "src/decompressed.txt");
                        statusLabel.setText("Decompression completed successfully.");
                    }
                    return;
                }else if(algorithm.equals("LZW")){
                    LZW lzw = new LZW();
                    if (isCompress) {
                        lzw.compress(inputFilePath, "src/compressed.txt");
                        statusLabel.setText("Compression completed successfully.");
                    } else {
                        lzw.decompress(inputFilePath, "src/decompressed.txt");
                        statusLabel.setText("Decompression completed successfully.");
                    }
                    return;
                }else if(algorithm.equals("Huffman")){
                    Huffman huffman = new Huffman();
                    if (isCompress) {
                        huffman.compress(inputFilePath, "src/compressed.bin");
                        statusLabel.setText("Compression completed successfully.");
                    } else {
                        huffman.decompress(inputFilePath, "src/decompressed.txt");
                        statusLabel.setText("Decompression completed successfully.");
                    }
                    return;
                } else if(algorithm.equals("Vector Quantization")){
                    Quantiz quantiz = new Quantiz();
                    if (isCompress) {
                        quantiz.compress(inputFilePath, "src/compressed.bin");
                        statusLabel.setText("Compression completed successfully.");
                    } else {
                        quantiz.decompress(inputFilePath, "src/decompressed.txt");
                        statusLabel.setText("Decompression completed successfully.");
                    }
                    return;
                }
            }
        });

        frame.setVisible(true);
    }
    public void run() {
        frame.setVisible(true);
    }
    
}
