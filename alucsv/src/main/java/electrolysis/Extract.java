package electrolysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class Extract {
	
	public void extractSingleGrooveRecord(String path,String toPathName,int certain) throws Exception{
		File directory = new File(path);
		String toPath=directory.getParent();
		StringBuffer sb=new StringBuffer();
		if (directory.isDirectory()) {
			File[] files = directory.listFiles();
			String second=null;
			int pos=0;
			for (File file : files) {
				System.out.println(file.getName());
				InputStream is =new FileInputStream(file);
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
			
				if(sb.length()==0){
					sb.append(br.readLine()+"\n");//读取标题
					//控制读取第几行，要跳过的行数
					for(int i=1; i<certain; i++){
						br.readLine();
					}
				
					second=br.readLine();//要读取的目标行
					pos = second.indexOf(",");
//					System.out.println(pos);
					sb.append(second+"\n");
				}
				else{
					//控制读取第几行，要跳过的行数
					for(int i=0; i<certain; i++){
						br.readLine();
					}
					String line=br.readLine();
					sb.append(line+"\n");
				}
				br.close();
			}
			StringBuffer name=new StringBuffer();
			name.append(toPath).append(File.separator).append(toPathName).append(File.separator)
			.append(second.substring(0, pos)).append(".csv");
			OutputStream os = new FileOutputStream(name.toString());
			os.write(sb.toString().getBytes());
			os.flush();
			os.close();
		} else {
			System.out.println("file path is wrong");
		}
	}
	
	public void extractMultipleGrooveRecord(String path, String toPathName,int count){
		for(int i=0;i<count;i++){
			try {
				extractSingleGrooveRecord(path, toPathName, i+1);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Extract ex=new Extract();
		String path="/home/ucas/software/aluminum-electrolysis/CSV日报/CSV二厂房日报/CSV二厂二区";
//		String toPathName="85.93-85-88-90";
//		String toPathName="85.96-85-88-90";
//		String toPathName="88.96-88-90-93";
		String toPathName="85.96-85-90-92";
		ex.extractMultipleGrooveRecord(path,toPathName,10);
		

	}

}
