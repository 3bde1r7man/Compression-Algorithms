import java.io.Serializable;

// class for matrix operations
public class VectorOperation implements Serializable {
	private static final long serialVersionUID = 1L;
	private double[][] average;

	
	public VectorOperation() 
	{

	}
	public VectorOperation(int size) 
	{
		average = new double[size][size];
		for (int i = 0; i < size; ++i) 
		{
			for (int j = 0; j < size; ++j) 
			{
				average[i][j] = 0.0;
			}
		}
	}
	public VectorOperation(VectorOperation another) 
	{
		average = new double[another.getArr().length][another.getArr()[0].length];
		for (int i = 0; i < another.getArr().length; ++i) 
		{
			for (int j = 0; j < another.getArr()[0].length; ++j) 
			{
				average[i][j] = another.getArr()[i][j];
			}
		}
	}

	public double[][] getArr()
	{
		return average;
	}
	public VectorOperation divide(int num) 
	{
		VectorOperation result = new VectorOperation(this.average.length);
		for (int i = 0; i < average.length; ++i) 
		{
			for (int j = 0; j < average[0].length; ++j) 
			{
				result.average[i][j] = average[i][j] / num;
			}
		}
		return result;
	}

	public VectorOperation plus(int[][] vec) {
		VectorOperation result = new VectorOperation(this.average.length);
		for (int i = 0; i < average.length; ++i)
		{
			for (int j = 0; j < average[0].length; ++j) 
			{
				result.average[i][j] = average[i][j] + vec[i][j];
			}
		}
		return result;

	}
	
	public double distance(int[][] another) 
	{
		double value = 0;
		for (int i = 0; i < average.length; ++i) 
		{
			for (int j = 0; j < average[0].length; ++j) 
			{
				value += Math.abs(average[i][j] - another[i][j]);
			}
		}
		return value;

	}

	public VectorOperation add(int num) {
		VectorOperation result = new VectorOperation(this.average.length);
		for (int i = 0; i < average.length; ++i) {
			for (int j = 0; j < average[0].length; ++j) {
				result.average[i][j] = average[i][j] + num;
			}
		}
		return result;
	}

	public VectorOperation floor() {
		VectorOperation result = new VectorOperation(this.average.length);
		for (int i = 0; i < average.length; ++i) {
			for (int j = 0; j < average[0].length; ++j) {
				result.average[i][j] = Math.floor(average[i][j]);
			}
		}
		return result;
	}

	public String toString() 
	{
		String output = "";
		for (int i = 0; i < average.length; ++i) 
		{
			for (int j = 0; j < average[0].length; ++j) 
			{
				output += average[i][j] + " ";
			}
			output += '\n';
		}
		return output;
	}
}
