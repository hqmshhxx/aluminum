package db;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class Todb {
	private String url = "jdbc:mysql://localhost:3306/aluminum?useSSL=false&characterEncoding=utf8";
	private String user = "root";
	private String password = "123456";
	private Connection conn = null;
	private PreparedStatement ps = null;
	private String sql = "insert into aldata(caoid,caoling,caoxing,yunxingshijian,jialiaoliang,jialiaocishu,"
			+ "dianyayusheding,dianyashijisheding,gongzuoshidianya,pingjundianya,zaosheng,yangjixingchengri,"
			+ "yangjixingchengdunlv,dianjiewendu,fenzibi,alfjialiaosheding,alfjialiaoshiji,lvshuiping,dianjiezhishuiping,"
			+ "shijichulvliang,zuorichulvzhishiliang,jinrichulvzhishiliang,fehanliang,sihanliang,dianliu,dianliuxiaolv,riqi,class)"
			+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,null)";

	{
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(url, user, password);
			ps = conn.prepareStatement(sql);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException se) {
			se.printStackTrace();
		}
	}

	public void service(String filePath) {
		List<String> list = null;
		File dir = new File(filePath);
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			try {
				conn.setAutoCommit(false);
				for (File file : files) {
					String fileName = file.getName();
					try {
						list = Files.readAllLines(file.toPath(),
								StandardCharsets.UTF_8);
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
					list.remove(0);
					toDb(fileName, list);
				}
				ps.executeBatch();
				conn.commit();
				System.out.println("insert success");
			} catch (SQLException sqle) {
				try {
					conn.rollback();
					System.out.println("insert false");
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				sqle.printStackTrace();
			} finally {
				try {
					ps.close();
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	public void testDb() throws SQLException {
		
			conn.setAutoCommit(false);
			ps.addBatch(sql);
			ps.setInt(1, 1);
	}
	public void toDb(String fileName, List<String> fileLines)
			throws SQLException {
		String dst = fileName.substring(0, fileName.length() - 4);
//		Date d = Date.valueOf(dst.substring(4));
		Date d = Date.valueOf(dst);
		for (String line : fileLines) {
			String[] strs = line.split(",");
			for (int i = 1; i <= strs.length; i++) {
				if (i == 1 || i == 4 || i == 5 || i == 6 || i == 11 || i == 12
						|| i == 13 || i == 14 || i == 16 || i == 17 || i == 18
						|| i == 19 || i == 20 || i == 21 || i == 22) {
					if (strs[i - 1].isEmpty()) {
						ps.setInt(i, 0);
					} else {
						ps.setInt(i, Integer.parseInt(strs[i - 1]));
					}

				} else if (i == 2 || i == 3) {
					ps.setString(i, strs[i - 1]);
				} else if (i == 27) {
					ps.setDate(i, d);
				} else {
					if (strs[i - 1].isEmpty()) {
						ps.setFloat(i, 0.0f);
					} else {
						float f = (float)Math.round(Float.parseFloat(strs[i - 1])*1000)/1000;
						ps.setFloat(i, f);
					}
				}
			}
			ps.addBatch();
		}
	}
	
	public static void main(String[] args) {
		Todb toDb = new Todb();
		String filePath = "/home/ucas/software/aluminum-electrolysis/CSV日报/CSV二厂房日报/CSV二厂二区/2016-1-1_2016-7-12";
		toDb.service(filePath);
		System.out.println("over");
	}

}
