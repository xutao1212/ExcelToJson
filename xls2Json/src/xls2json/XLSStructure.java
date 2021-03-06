package xls2json;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.poi.*;
import org.apache.poi.hssf.usermodel.HSSFShape;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import com.alibaba.fastjson.*;

public class XLSStructure {
	public String xlsFileName;
	public ArrayList<String> xlsSheets;
	public static int Line_Description = 0;
	public static int Line_Name = 1;
	public static int Line_Type = 2;
	public static int Line_ValueBegin = 3;

	public XLSStructure(String thisFileName) {
		xlsFileName = thisFileName;
		xlsSheets = new ArrayList<String>();
		System.out.println("Construct new XlsStructure:" + thisFileName);

	}

	protected void OutputJson_withoutMainKey(HSSFSheet sheet, String outputName) {
		// JSONObject json=new JSONObject();
		JSONArray sheetArray = new JSONArray();
		Row rowDesp = sheet.getRow(Line_Description);
		Row rowName = sheet.getRow(Line_Name);
		Row rowType = sheet.getRow(Line_Type);

		// Cell testCell=sheet.getRow(2).getCell(2);
		// System.out.println(testCell.getStringCellValue());
		// int numValueLines=sheet.getLastRowNum()-Line_ValueBegin;
		int numColumes = rowName.getPhysicalNumberOfCells();
		System.out.println("Processing sheet:" + sheet.getSheetName() + " rowNum is "+sheet.getLastRowNum()+" colNum is "+numColumes + " ...");
		for (int i = Line_ValueBegin; i <= sheet.getLastRowNum(); i++) {
			String l_stringLineTip = "lineNum:"+(i+1)+"...";
			Row thisRow = sheet.getRow(i);
			//允许有空行
			//默认第一列为主键
			String l_strMainKey = thisRow.getCell(0).getStringCellValue();
			if(l_strMainKey.trim().equals("")){
				l_stringLineTip += "empty line";
				see(l_stringLineTip);
				continue;
			}
			see(l_stringLineTip);
						
			JSONObject lineJsonObject = new JSONObject();
			for (int j = 0; j < numColumes; j++) {
				//System.out.println("colume:" + j + "..." + rowDesp.getCell(j).getStringCellValue());
				char thisType = rowType.getCell(j).getStringCellValue()
						.charAt(0);
				switch (thisType) {
				//注释,给策划看的,不导出json数据
				case 'N':
					break;
					
				//字符串
				case 's': {
					lineJsonObject.put(rowName.getCell(j).getStringCellValue(),
							thisRow.getCell(j).getStringCellValue());
					break;
				}
				//整型或浮点型
				case 'i':
				case 'f': {
					lineJsonObject.put(rowName.getCell(j).getStringCellValue(),
							thisRow.getCell(j).getNumericCellValue());
					break;
				}
				//数值数组
				case 'a': {
					JSONArray cellArray = new JSONArray();
					String arrayValue = thisRow.getCell(j).getStringCellValue();
					String[] arr = arrayValue.split("\\|");
					for (int k = 0; k < arr.length; k++) {
						cellArray.add(Float.parseFloat(arr[k]));
					}
					lineJsonObject.put(rowName.getCell(j).getStringCellValue(),
							cellArray);
					break;
				}
				//字体串数组
				case 'A': {
					JSONArray cellArray = new JSONArray();
					String arrayValue = thisRow.getCell(j).getStringCellValue();
					String[] arr = arrayValue.split("\\|");
					for (int k = 0; k < arr.length; k++) {
						cellArray.add(arr[k]);
					}
					lineJsonObject.put(rowName.getCell(j).getStringCellValue(),
							cellArray);
					break;
				}
				default:
					break;
				}
			}
			//
			sheetArray.add(lineJsonObject);
		}
		WriteJsonArray(sheetArray, outputName);
		System.out.println("======================================");
		System.out.println("Output Json Succeed!! Check the file at:" + outputName);
		System.out.println("======================================");
	}

	protected void OutputJson_withMainKey(HSSFSheet sheet, String outputName) {
		JSONObject jsonRoot = new JSONObject();
		Row rowDesp = sheet.getRow(Line_Description);
		Row rowName = sheet.getRow(Line_Name);
		Row rowType = sheet.getRow(Line_Type);

		// Cell testCell=sheet.getRow(2).getCell(2);
		// System.out.println(testCell.getStringCellValue());
		// int numValueLines=sheet.getLastRowNum()-Line_ValueBegin;
		int numColumes = rowName.getPhysicalNumberOfCells();
		System.out.println("Processing sheet:" + sheet.getSheetName() + " rowNum is "+sheet.getLastRowNum()+" colNum is "+numColumes + " ...");
		for (int i = Line_ValueBegin; i <= sheet.getLastRowNum(); i++) {
			String l_stringLineTip = "lineNum:"+(i+1)+"...";
			Row thisRow = sheet.getRow(i);								
			//默认第一列为主键
			String l_strMainKey = thisRow.getCell(0).getStringCellValue();
			if(l_strMainKey.trim().equals("")){
				l_stringLineTip += "empty line";
				see(l_stringLineTip);
				continue;
			}
			see(l_stringLineTip);
			
			JSONObject lineJsonObject = new JSONObject();
			for (int j = 1; j < numColumes; j++) {
				//System.out.println("colume:" + j + "..." + rowDesp.getCell(j).getStringCellValue());
				char thisType = rowType.getCell(j).getStringCellValue()
						.charAt(0);
				switch (thisType) {
				//注释,给策划看的,不导出json数据
				case 'N':
					break;
					
				//字符串
				case 's': {
					lineJsonObject.put(rowName.getCell(j).getStringCellValue(),
							thisRow.getCell(j).getStringCellValue());
					break;
				}
				//整型或浮点型
				case 'i':
				case 'f': {
					lineJsonObject.put(rowName.getCell(j).getStringCellValue(),
							thisRow.getCell(j).getNumericCellValue());
					break;
				}
				//数值数组
				case 'a': {
					JSONArray cellArray = new JSONArray();
					String arrayValue = thisRow.getCell(j).getStringCellValue();
					String[] arr = arrayValue.split("\\|");
					for (int k = 0; k < arr.length; k++) {
						cellArray.add(Float.parseFloat(arr[k]));
					}
					lineJsonObject.put(rowName.getCell(j).getStringCellValue(),
							cellArray);
					break;
				}
				//字体串数组
				case 'A': {
					JSONArray cellArray = new JSONArray();
					String arrayValue = thisRow.getCell(j).getStringCellValue();
					String[] arr = arrayValue.split("\\|");
					for (int k = 0; k < arr.length; k++) {
						cellArray.add(arr[k]);
					}
					lineJsonObject.put(rowName.getCell(j).getStringCellValue(),
							cellArray);
					break;
				}
				default:
					break;
				}
			}
			//
			jsonRoot.put(l_strMainKey, lineJsonObject);
			//sheetArray.add(lineJsonObject);
		}
		//WriteJsonArray(sheetArray, outputName);
		WriteJsonObject(jsonRoot, outputName);
		System.out.println("======================================");
		System.out.println("Output Json Succeed!! Check the file at:" + outputName);
		System.out.println("======================================");
	}
	
	public void WriteJsonArray(JSONArray sheetArray, String outputName) {

		String jsonString = JSONArray.toJSONString(sheetArray, true);
		try {
			File f = new File(outputName);
			if (!f.exists()) {
				f.createNewFile();
			}
			BufferedWriter outputBufferedWriter = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
			outputBufferedWriter.write(jsonString);
			outputBufferedWriter.close();
		} catch (Exception e) {
			// TODO: handle exception
		}

	}
	
	public void WriteJsonObject(JSONObject sheetObject, String outputName) {

		String jsonString = JSONObject.toJSONString(sheetObject, true);
		try {
			File f = new File(outputName);
			if (!f.exists()) {
				f.createNewFile();
			}
			BufferedWriter outputBufferedWriter = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
			outputBufferedWriter.write(jsonString);
			outputBufferedWriter.close();
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	public void AnalyzeJson(String outputPath) {
		POIFSFileSystem fs;
		try {
			fs = new POIFSFileSystem(new FileInputStream(xlsFileName));
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			for (int i = 0; i < xlsSheets.size(); i++) {
				String sheetName = xlsSheets.get(i);
				HSSFSheet sheet = wb.getSheet(sheetName);
				if(Entrance.s_bIsWithMainKey){
					OutputJson_withMainKey(sheet, outputPath + sheetName + ".txt");
				}else{
					OutputJson_withoutMainKey(sheet, outputPath + sheetName + ".txt");
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void see(String p_stringContent){
		System.out.println(p_stringContent);
	}
}