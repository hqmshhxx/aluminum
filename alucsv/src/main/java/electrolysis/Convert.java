package electrolysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;

public class Convert {

	private String titles;

	public void readExcelTitle(String fileName) throws Exception{
		InputStream is = new FileInputStream(fileName);
		InputStreamReader isr= new InputStreamReader(is);
		BufferedReader br=new BufferedReader(isr);
		titles=br.readLine();
		br.close();
		
	}

	public String readExcelContent(File file) throws Exception {
		InputStream is = new FileInputStream(file);

		POIFSFileSystem fs = new POIFSFileSystem(is);
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		HSSFSheet sheet = wb.getSheetAt(0);
		HSSFRow row;
		HSSFCell cell;
		String value = null;
		StringBuffer buffer = new StringBuffer();
		buffer.append(file.getName()+"\n");
		System.out.println(file.getName());
		
		//读取系列电流
		row = sheet.getRow(1);
		cell=row.getCell(30);
		value=cell.getStringCellValue();
		int index =value.indexOf('1');
		String electricity=value.substring(index, index+6);
		
		int rowNum = sheet.getLastRowNum();
		int colNum = 0;
		int col = 0;
	
		// int colNum = sheet.getPhysicalNumberOfRows();
		for (int i = 7; i < rowNum - 9; i++) {
			row = sheet.getRow(i);
			colNum = row.getPhysicalNumberOfCells();
			col = row.getLastCellNum();
		
			// System.out.println("colNum="+colNum+" lastcellnum="+col);
			if (colNum == 39) {
				for (int j = 0; j < colNum; j++) {
					if (j < 2 || (j > 2 && j < 9) || j == 10 || j == 13 || j == 20 || (j > 21 && j < 28) || j == 29
							|| (j > 31 && j < 38)) {
						cell = row.getCell(j);

						if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
							value = String.valueOf(cell.getNumericCellValue());
						} else {
							value = cell.getStringCellValue();
						}
						int poia = value.indexOf("+");
						int poib = value.indexOf("-");
						String val=null;
						if (poia > 0) {
							val=value.substring(0, poia);
						} else if (poib > 0) {
							val=value.substring(0, poib);
						} else {
							val=value;
						}
						buffer.append(val);
						buffer.append(",");
					
					}
				}
				buffer.append(electricity);
				buffer.append("\n");
			}
		}
		fs.close();
		is.close();
		return buffer.toString();
		
	}
	public void printCSV(String content,String toPath) throws Exception{
		String[] lines =content.split("\n");
		String oldFileName= lines[0];
		int poi=oldFileName.indexOf(".");
		String newFileName=oldFileName.substring(0,poi)+".csv";
		OutputStream os =new FileOutputStream(toPath+"/"+newFileName);
		OutputStreamWriter osw = new OutputStreamWriter(os);
		BufferedWriter bw =new BufferedWriter(osw);
		bw.write(titles);
		for(int i=1;i<lines.length;i++){
			String[] tokens = lines[i].split(",");
			if(tokens.length>4){
				if(!tokens[1].equalsIgnoreCase("stop")){
					bw.newLine();
					bw.write(lines[i]);
				}
			}
		}
		bw.flush();
		bw.close();
	}

	public void convertExcelFiles(String path,String toPath) throws Exception {
		File directory = new File(path);
		if (directory.isDirectory()) {
			File[] files = directory.listFiles();
			for (File file : files) {
			 String content=readExcelContent(file);
			 printCSV(content,toPath);
			}
		} else {
			System.out.println("file path is wrong");

		}

	}
	public void extractSingleGrooveRecord(String path,String toPath) throws Exception{
		File directory = new File(path);
		StringBuffer sb=new StringBuffer();
		if (directory.isDirectory()) {
			File[] files = directory.listFiles();
			String second=null;
			int pos=0;
			for (File file : files) {
				InputStream is =new FileInputStream(file);
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				if(sb.length()==0){
					sb.append(br.readLine()+"\n");
//					br.readLine();//控制读取第几行，要跳过的行数
					second=br.readLine();//控制读取第几行
					pos = second.indexOf(",");
					sb.append(second+"\n");
				}
				else{
					br.readLine();
					String line=br.readLine();
					sb.append(line+"\n");
				}
				br.close();
			}
			StringBuffer name=new StringBuffer();
			name.append(toPath).append("/").append(second.substring(0, pos)).append(".csv");
			OutputStream os = new FileOutputStream(name.toString());
			os.write(sb.toString().getBytes());
			os.flush();
			os.close();
		} else {
			System.out.println("file path is wrong");

		}
	}

	public static void main(String[] args) {
		Convert convert = new Convert();
		try {
	
			convert.readExcelTitle("/home/ucas/software/aluminium-electrolysis/1-1-2015-10-1-csv.csv");
			convert.convertExcelFiles(
					"/home/ucas/software/aluminium-electrolysis/二厂房日报/二厂二区", 
					"/home/ucas/software/aluminium-electrolysis/CSV日报/CSV二厂房日报/CSV二厂二区-temp");


		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
