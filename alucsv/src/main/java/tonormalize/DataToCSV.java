package tonormalize;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class DataToCSV {
	public void convert(String from,String to){
		List<String> contents =null;
		StringBuilder sb = new StringBuilder();
		try {
			contents = Files.readAllLines(Paths.get(from));
			for(String line : contents){
				String[] charts = line.split(" ");
				for(int i=0; i < charts.length; i++){
					if(i < charts.length-1){
						if(!charts[i].isEmpty()){
							sb.append(charts[i].trim()).append(",");
						}
					
					}else{
						if(!charts[i].isEmpty()){
							sb.append(charts[i]).append("\n");
						}
					}
				}
			}
			Files.write(Paths.get(to), sb.toString().getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DataToCSV cv = new DataToCSV();
		String base = System.getProperty("user.dir");
		String from = base+"/src/main/resources/dataset/pop_failures.dat";
		String to = base+"/src/main/resources/dataset/pop_failures.arff";
		cv.convert(from, to);
	}
}
