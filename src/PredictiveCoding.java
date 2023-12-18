import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class PredictiveCoding {
    
    public static int[][] readImage(String filePath) {
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

    public void compress(String inPath, String outPath) {
        int img[][] = readImage(inPath);
        
        int predicted[][] = predictor(img);
        int residuals[][] = residuals(img, predicted);
        int quantized[][] = qunatize(residuals);
        writeImg(quantized);
        //writeImg(residuals);
    }
    private int[][]read(String inPath){
        int[][] img = new int[3][3];
        try{
            BufferedReader reader = new BufferedReader(new FileReader(inPath));            
            String line = "";
            int j = 0;
            while(reader.ready()){
                line = reader.readLine();
                String[] arr = line.split(" ");
                for(int i = 0; i < 3;i++){
                    img[j][i] =  Integer.parseInt(arr[i]);
                }
                j++;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return img;
    }

    public void decompress(String inPath, String outPath) {
        int quantized[][] = null;
        try{
            FileInputStream fis = new FileInputStream(inPath);
            ObjectInputStream in = new ObjectInputStream(fis);
            quantized = (int[][]) in.readObject();
            in.close();
            fis.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        int residuals[][] = dequantize(quantized);
        int predicted[][] = depredictor(residuals);
        //int predicted[][] = depredictor(quantized);
        writeImage(predicted, outPath);
        
    }

    private int[][] dequantize(int quantized[][]){
        int residuals[][] = new int[quantized.length][quantized[0].length];
        for(int i = 0; i < quantized.length; ++i){
            for(int j = 0; j < quantized[0].length; ++j){
                if(i == 0 && j == 0){
                    residuals[i][j] = quantized[i][j];
                    continue;
                }
                if(quantized[i][j]  == -8){
                    residuals[i][j] = (-255 + -224) / 2;
                }else if(quantized[i][j]  == -7){
                    residuals[i][j] = (-223 + -192) / 2;
                }else if(quantized[i][j]  == -6){
                    residuals[i][j] = (-191 + -160) / 2;
                }else if(quantized[i][j]  == -5){
                    residuals[i][j] = (-159 + -128) / 2;
                }else if(quantized[i][j]  == -4){
                    residuals[i][j] = (-127 + -96) / 2;
                }else if(quantized[i][j]  == -3){
                    residuals[i][j] = (-95 + -64) / 2;
                }else if(quantized[i][j]  == -2){
                    residuals[i][j] = (-63 + -32) / 2;
                }else if(quantized[i][j]  == -1){
                    residuals[i][j] = (-31 + 0) / 2;
                }else if(quantized[i][j]  == 0){
                    residuals[i][j] = (1 + 32) / 2;
                }else if(quantized[i][j]  == 1){
                    residuals[i][j] = (33 + 64) / 2;
                }else if(quantized[i][j]  == 2){
                    residuals[i][j] = (65 + 96) / 2;
                }else if(quantized[i][j]  == 3){
                    residuals[i][j] = (97 + 128) / 2;
                }else if(quantized[i][j]  == 4){
                    residuals[i][j] = (129 + 160) / 2;
                }else if(quantized[i][j]  == 5){
                    residuals[i][j] = (161 + 192) / 2;
                }else if(quantized[i][j]  == 6){
                    residuals[i][j] = (193 + 224) / 2;
                }else if(quantized[i][j]  == 7){
                    residuals[i][j] = (225 + 255) / 2;
                }
            }
        }
        return residuals;
    }

    private int[][] depredictor(int residuals[][]){
        int predicted[][] = new int[residuals.length][residuals[0].length];
        for(int i = 0; i < residuals.length; ++i){
            for(int j = 0; j < residuals[0].length; ++j){
                if(i == 0 && j == 0){
                    predicted[i][j] = 0;
                }
                else if(j == 0){
                    predicted[i][j] = predicted[i - 1][j];
                }
                else{
                    predicted[i][j] = predicted[i][j - 1];
                }
                predicted[i][j] += residuals[i][j];
                if(predicted[i][j] < 0){
                    predicted[i][j] = 0;
                }
                else if(predicted[i][j] > 255){
                    predicted[i][j] = 255;
                }
            }
        }
        return predicted;
    }

    private void writeImage(int[][] predicted, String outPath) {
        int width = predicted[0].length;
        int height = predicted.length;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        File file = new File(outPath);
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; y++) {
                // write thee image pixel by pixel in gray scale
                int p = (predicted[y][x] << 16) | (predicted[y][x] << 8) | predicted[y][x] ;
                image.setRGB(x, y, p);
            }
        }
        
        try {
            ImageIO.write(image, "jpg", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int[][] predictor(int img[][]){
        int predicted[][] = new int[img.length][img[0].length];
        for(int i = 0; i < img.length; ++i){
            for(int j = 0; j < img[0].length; ++j){
                if(i == 0 && j == 0){
                    predicted[i][j] = 0;
                }
                else if(j == 0){
                    predicted[i][j] = img[i - 1][j];
                }
                else{
                    predicted[i][j] = img[i][j - 1];
                }
            }
        }
        return predicted;
    }

    private int[][] residuals(int img[][], int predicted[][]){
        int residuals[][] = new int[img.length][img[0].length];
        for(int i = 0; i < img.length; ++i){
            for(int j = 0; j < img[0].length; ++j){
                residuals[i][j] = img[i][j] - predicted[i][j];
            }
        }
        return residuals;
    }

    private int[][] qunatize(int residuals[][]){
        int quantized[][] = new int[residuals.length][residuals[0].length];
        for(int i = 0; i < residuals.length; ++i){
            for(int j = 0; j < residuals[0].length; ++j){
                if(i == 0 && j == 0){
                    quantized[i][j] = residuals[i][j];
                    continue;
                }
                if(residuals[i][j] >= -255 && residuals[i][j] <= -224){
                    quantized[i][j] = -8;
                }else if(residuals[i][j] >= -223 && residuals[i][j] <= -192){
                    quantized[i][j] = -7;
                }else if(residuals[i][j] >= -191 && residuals[i][j] <= -160){
                    quantized[i][j] = -6;
                }else if(residuals[i][j] >= -159 && residuals[i][j] <= -128){
                    quantized[i][j] = -5;
                }else if(residuals[i][j] >= -127 && residuals[i][j] <= -96){
                    quantized[i][j] = -4;
                }else if(residuals[i][j] >= -95 && residuals[i][j] <= -64){
                    quantized[i][j] = -3;
                }else if(residuals[i][j] >= -63 && residuals[i][j] <= -32){
                    quantized[i][j] = -2;
                }else if(residuals[i][j] >= -31 && residuals[i][j] <= 0){
                    quantized[i][j] = -1;
                }else if(residuals[i][j] >= 1 && residuals[i][j] <= 32){
                    quantized[i][j] = 0;
                }else if(residuals[i][j] >= 33 && residuals[i][j] <= 64){
                    quantized[i][j] = 1;
                }else if(residuals[i][j] >= 65 && residuals[i][j] <= 96){
                    quantized[i][j] = 2;
                }else if(residuals[i][j] >= 97 && residuals[i][j] <= 128){
                    quantized[i][j] = 3;
                }else if(residuals[i][j] >= 129 && residuals[i][j] <= 160){
                    quantized[i][j] = 4;
                }else if(residuals[i][j] >= 161 && residuals[i][j] <= 192){
                    quantized[i][j] = 5;
                }else if(residuals[i][j] >= 193 && residuals[i][j] <= 224){
                    quantized[i][j] = 6;
                }else if(residuals[i][j] >= 225 && residuals[i][j] <= 255){
                    quantized[i][j] = 7;
                }
            }
        }
        return quantized;
    }

    private void writeImg(int[][] quantized){
        try{
            FileOutputStream fos = new FileOutputStream("src/compressed.bin");
            ObjectOutputStream out = new ObjectOutputStream(fos);
            out.writeObject(quantized);
            out.close();
            fos.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}