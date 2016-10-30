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
import java.util.ArrayList;
import java.util.List;

public class Todb {
	private String url = "jdbc:mysql://localhost:3306/aluminum";
	private String user = "root";
	private String password = "123456";
	private Connection conn = null;
	private PreparedStatement ps = null;
	private String sql = "insert into aldata(cao_id,cao_ling,cao_xing,yunxingshijian,jialiaoliang,jialiaocishu,"
			+ "dianyayusheding,dianyashijisheding,gongzuoshidianya,pingjundianya,dianliu,zaosheng,yangjixingchengri,"
			+ "yangjixingchengdunlv,dianjiewendu,fenzibi,alfjialiaosheding,alfjialiaoshiji,lvshuiping,dianjiezhishuiping,"
			+ "fehanliang,sihanliang,shijichulvliang,zuorichulvzhishiliang,jinrichulvzhishiliang,riqi,dianliuxiaolv,class)"
			+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

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
						list = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
					toDb(fileName, list);
				}
				ps.executeBatch();
				conn.commit();
			} catch (SQLException sqle) {
				try {
					conn.rollback();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				sqle.printStackTrace();
			}finally{
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

	public List<String> readCsv(File file) {
		List<String> fileLines = null;
		try {
			fileLines = Files.readAllLines(file.toPath(),
					StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileLines;
	}

	public void toDb(String fileName, List<String> fileLines)
			throws SQLException {
		String dst = fileName.substring(0, fileName.length() - 3);
		Date d = Date.valueOf(dst.substring(4));
		
		for (String line : fileLines) {
			String[] strs = line.split(",");
			ps.addBatch(sql);
			for (int i = 1; i <= strs.length; i++) {
				if (i == 1 || i == 4 || i == 5 || i == 6 || i == 12 || i == 13
						|| i == 14 || i == 15 || i == 17 || i == 18 || i == 19
						|| i == 20 || i == 23 || i == 24 || i == 25) {
					ps.setInt(i, Integer.parseInt(strs[i]));
				} else if (i == 2 || i == 3 || i == 28) {
					ps.setString(i, strs[i]);
				} else if (i == 26) {
					ps.setDate(i, d);
				} else {
					ps.setFloat(i, Float.parseFloat(strs[i]));
				}
			}
		}
	}

	public static void main(String[] args) {

	}

}
