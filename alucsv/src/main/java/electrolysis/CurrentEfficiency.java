package electrolysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CurrentEfficiency {
	

	public double calculateEfficiency(String line){
		String[] array = line.split(",");
		double r=0.0;
//		System.out.println(array[0]+"  "+array[4]+"  "+array[20]);
		if(array.length>20){
			if(!array[19].isEmpty()&& !array[3].isEmpty()&& !array[24].isEmpty()){
				double p=Double.parseDouble(array[19]);
				double electricity = Double.parseDouble(array[24]);
				double time = Double.parseDouble(array[3])/60;
				r = 100*p/(0.3355*electricity*time);
			}else{
				r=Double.NaN;
			}
		}else{
			r=Double.NaN;
		}
	
		return r;
	}
	public String readCSV(File file){
		StringBuilder sb =new StringBuilder();
		StringBuilder title=new StringBuilder();
		BufferedReader br=null;
		try {
			br = new BufferedReader(new FileReader(file));
			title.append(br.readLine());
			title.append(",");
			title.append("出铝电流效率");
			title.append(",");
			title.append("类别");
			title.append("\n");
			sb.append(title.toString());
			String line;
			while((line=br.readLine())!=null){
				double r=calculateEfficiency(line);
				if(line.length()>40){
					sb.append(line);
					sb.append(",");
				}
			
				if(Double.isNaN(r)){
//					sb.append("");
				}else{
				
					sb.append(r);
					sb.append(",");
					if(r>=92.0&&r<=99){
						sb.append("优秀");
					}
					else if(r>=90.0&&r<92.0){
						sb.append("良好");
					}
					else if (r>=85.0&&r<90.0){
						sb.append("中等");
					}
				}
				if(line.length()>40){
					sb.append("\n");
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			try {
				if(br!=null){
					br.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	return sb.toString();
	}
	
	public void printCSV(String sb, String fileName){
		BufferedWriter bw=null;
		try {
			bw = new BufferedWriter(new FileWriter(fileName));
			bw.write(sb);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
				try {
					if(bw!=null)
					bw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	public void cce(String path,String toPath) throws Exception {
		File directory = new File(path);
		if (directory.isDirectory()) {
			File[] files = directory.listFiles();
			for (File file : files) {
				String cont=readCSV(file);
				String fileName=toPath+File.separator+file.getName();
				printCSV(cont,fileName);
		}
		} else {
			System.out.println("file path is wrong");

		}

	}

	public static void main(String[] args){
		CurrentEfficiency ce =new CurrentEfficiency();
		try {
			ce.cce("/home/ucas/software/aluminium-electrolysis/CSV日报/CSV二厂房日报/CSV二厂二区-temp", 
					"/home/ucas/software/aluminium-electrolysis/CSV日报/CSV二厂房日报/CSV二厂二区");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			System.out.println("over");
		}
	}
}