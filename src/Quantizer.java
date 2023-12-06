import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;
import javax.imageio.ImageIO;

class Quantizer {

	// INPATH is image name.png or jpeg  
	public static void compressImg(String inPath, String outPath, int vectorSize, int numberOfBits)
			throws IOException {

		int img[][] = readImage(inPath);
		Vector<int[][]> imgData = split(img, vectorSize);
		Vector<AverageVector> values = qunatize(imgData, numberOfBits);
		Vector<Integer> output = new Vector<>();
		for (int i = 0; i < imgData.size(); ++i) {
			int index = close(imgData.get(i), values);
			output.add(index);
		}

		FileOutputStream fos = new FileOutputStream("compressed.bin");
		ObjectOutputStream out = new ObjectOutputStream(fos);
		out.writeObject(values);
		out.writeObject(output);
		out.close();
	}

	public static AverageVector average(Vector<int[][]> data) {
		int size = data.get(0).length;
		AverageVector avg = new AverageVector(size);
		for (int i = 0; i < data.size(); ++i) {
			for (int r = 0; r < size; ++r) {
				for (int c = 0; c < size; ++c) {
					avg.getArr()[r][c] += data.get(i)[r][c];
				}
			}
		}
		return avg.divide(data.size());
	}

	public static int close(int[][] n, Vector<AverageVector> values) {

		double c = values.get(0).distance(n);
		int index = 0;
		for (int i = 1; i < values.size(); ++i) {
			if (Double.compare(c, values.get(i).distance(n)) > 0) {
				c = values.get(i).distance(n);
				index = i;
			}
		}
		return index;
	}

	public static void associate(Vector<int[][]> data, Vector<AverageVector> averages) {
		Vector<AverageVector> sums = new Vector<>();
		Vector<Integer> counters = new Vector<>();
		for (int i = 0; i < averages.size(); ++i) {
			sums.add(new AverageVector(data.get(0).length));
			counters.add(0);
		}

		for (int i = 0; i < data.size(); ++i) {
			int index = close(data.get(i), averages);
			sums.setElementAt(sums.get(index).plus(data.get(i)), index); // sum[index] += data[i]
			counters.setElementAt(counters.get(index) + 1, index); // i++
		}
		for (int i = 0; i < averages.size(); ++i) {
			averages.setElementAt(sums.get(i).divide(counters.get(i)), i);
		}
	}

	public static Vector<AverageVector> qunatize(Vector<int[][]> data, int numberOfBits) {

		AverageVector avg = average(data);
		Vector<AverageVector> values = new Vector<>();
		values.add(avg);
		while (values.size() < numberOfBits) {
			int size = values.size();
			for (int j = 0; j < size; ++j) {
				AverageVector current = values.remove(0);
				AverageVector low = current.floor();
				AverageVector high = low.add(1);
				values.add(low);
				values.add(high);
			}
			associate(data, values);
		}
		associate(data, values);
		return values;
	}

	public static void decompress(String inPath,String outPath) throws IOException, ClassNotFoundException {

		FileInputStream fis = new FileInputStream(inPath);
		ObjectInputStream in = new ObjectInputStream(fis);
		@SuppressWarnings("unchecked")
		Vector<AverageVector> averages = (Vector<AverageVector>) in.readObject();
		@SuppressWarnings("unchecked")
		Vector<Integer> compImage = (Vector<Integer>) in.readObject();
		in.close();

		Vector<AverageVector> linearImage = new Vector<>();
		for (int j = 0; j < compImage.size(); ++j) {
			AverageVector temp = new AverageVector(averages.get(compImage.get(j)));
			linearImage.add(temp);
		}
		int vectorSize = averages.get(0).getArr().length; //2
		int y = (int)Math.sqrt(compImage.size()) * vectorSize;
		int image[][] = new int[y][y];
		int i = 0, j = 0;
		for (int n = 0; n < linearImage.size(); ++n) {
			if (j == 0 && n != 0) {
				i = (i + vectorSize) % y;
			}
			for (int r = i; r < (i + vectorSize); ++r) {
				for (int c = j; c < (j + vectorSize); ++c) {
					image[r][c] = (int) linearImage.get(n).getArr()[r - i][c - j];
				}
			}
			j = (j + vectorSize) % y;
		}
		writeImage(image, null, outPath, y, y);
	}

	public static Vector<int[][]> split(int img[][], int vectorSize) {
		Vector<int[][]> splitted = new Vector<>();
		for (int i = 0; i < img.length; i += vectorSize) {
			for (int j = 0; j < img[0].length; j += vectorSize) {
				int[][] block = new int[vectorSize][vectorSize];
				for (int r = i; r < (i + vectorSize); ++r) {
					for (int c = j; c < (j + vectorSize); ++c) {
						block[r - i][c - j] = img[r][c];
					}
				}
				splitted.add(block);
			}
		}
		return splitted;
	}

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

	public static void writeImage(int[][] pixels, Vector<Integer> output, String outputFilePath, int height,
			int width) {
				// reverse engineer the readImage method
		if (output != null) {
			pixels = new int[height][width];
			for (int i = 0; i < output.size(); ++i) {
				int r = i / width;
				int c = i % width;
				pixels[r][c] = output.get(i);
			}
		}
		File fileout = new File(outputFilePath);
		BufferedImage image2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				image2.setRGB(x, y, (pixels[y][x] << 16) | (pixels[y][x] << 8) | (pixels[y][x]));
			}
		}
		try {
			ImageIO.write(image2, "jpg", fileout);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
